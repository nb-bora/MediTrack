package cm.pharma.contexts.stocks_tracabilite.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.InventaireJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.InventaireJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.InventaireLigneJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.InventaireLigneJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.LotStockJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.LotStockJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.MouvementStockJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.MouvementStockJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.StockEmplacementJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.StockEmplacementJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValiderInventaireUseCase {

    private final InventaireJpaRepository inventaires;
    private final InventaireLigneJpaRepository lignes;
    private final LotStockJpaRepository lots;
    private final StockEmplacementJpaRepository stock;
    private final MouvementStockJpaRepository mouvements;
    private final AuditWriter auditWriter;

    public ValiderInventaireUseCase(
            InventaireJpaRepository inventaires,
            InventaireLigneJpaRepository lignes,
            LotStockJpaRepository lots,
            StockEmplacementJpaRepository stock,
            MouvementStockJpaRepository mouvements,
            AuditWriter auditWriter
    ) {
        this.inventaires = Objects.requireNonNull(inventaires);
        this.lignes = Objects.requireNonNull(lignes);
        this.lots = Objects.requireNonNull(lots);
        this.stock = Objects.requireNonNull(stock);
        this.mouvements = Objects.requireNonNull(mouvements);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public void execute(UUID organisationId, UUID inventaireId, UUID validatedBy) {
        InventaireJpaEntity inv = inventaires.findById(inventaireId)
                .orElseThrow(() -> new BusinessRuleViolationException("Inventaire introuvable"));
        if (!inv.getOrganisationId().equals(organisationId)) {
            throw new BusinessRuleViolationException("Inventaire hors organisation");
        }
        if (!"OUVERT".equals(inv.getStatut())) {
            throw new BusinessRuleViolationException("Inventaire non validable");
        }

        Instant now = Instant.now();

        for (InventaireLigneJpaEntity ligne : lignes.findByInventaireId(inventaireId)) {
            if (ligne.getStockReel() == null) {
                throw new BusinessRuleViolationException("Toutes les lignes doivent être saisies avant validation");
            }
            int ecart = ligne.getEcart() == null ? 0 : ligne.getEcart();
            if (ecart == 0) {
                continue;
            }
            if (ligne.getMotifEcart() == null || ligne.getMotifEcart().isBlank()) {
                throw new BusinessRuleViolationException("Motif obligatoire en cas d’écart");
            }

            if (ecart < 0) {
                appliquerEcartNegatif(organisationId, inventaireId, validatedBy, now, ligne, -ecart);
            } else {
                appliquerEcartPositif(organisationId, inventaireId, validatedBy, now, ligne, ecart);
            }
        }

        inv.valider(validatedBy, now);

        auditWriter.write(AuditEvent.simple(
                organisationId, now, null, null, null,
                null, null, "INVENTAIRE_VALIDE", "Inventaire", inventaireId.toString(), null,
                Map.of()
        ));
    }

    private void appliquerEcartNegatif(UUID organisationId, UUID inventaireId, UUID validatedBy, Instant now, InventaireLigneJpaEntity ligne, int aRetirer) {
        var lotsRows = stock.findLotsActifsDisponibles(organisationId, ligne.getEmplacementId(), ligne.getProduitId()).stream()
                .sorted(Comparator.comparing(StockEmplacementJpaRepository.StockLotDisponibleRow::getDatePeremption)
                        .thenComparing(StockEmplacementJpaRepository.StockLotDisponibleRow::getNumeroLot))
                .toList();
        int remaining = aRetirer;
        for (var row : lotsRows) {
            if (remaining <= 0) {
                return;
            }
            int take = Math.min(row.getQuantite(), remaining);
            StockEmplacementJpaEntity se = stock.findByOrganisationIdAndEmplacementIdAndLotId(organisationId, ligne.getEmplacementId(), row.getLotId())
                    .orElseThrow(() -> new BusinessRuleViolationException("Stock introuvable (concurrence)"));
            se.setQuantite(se.getQuantite() - take, now);
            stock.save(se);
            mouvements.save(MouvementStockJpaEntity.create(
                    new MouvementStockJpaEntity.MouvementInit(
                            UUID.randomUUID(),
                            organisationId,
                            "AJUSTEMENT",
                            row.getLotId(),
                            ligne.getProduitId(),
                            take,
                            ligne.getEmplacementId(),
                            null,
                            "INV-" + inventaireId,
                            ligne.getMotifEcart(),
                            validatedBy,
                            now
                    )
            )));
            remaining -= take;
        }
        if (remaining > 0) {
            throw new BusinessRuleViolationException("Impossible d’appliquer l’ajustement (stock insuffisant) — recommencer");
        }
    }

    private void appliquerEcartPositif(UUID organisationId, UUID inventaireId, UUID validatedBy, Instant now, InventaireLigneJpaEntity ligne, int aAjouter) {
        String numeroLot = ("AJUST-" + inventaireId).toUpperCase();
        UUID lotId = lots.findByOrganisationIdAndProduitIdAndNumeroLot(organisationId, ligne.getProduitId(), numeroLot)
                .map(LotStockJpaEntity::getId)
                .orElseGet(() -> {
                    UUID id = UUID.randomUUID();
                    lots.save(LotStockJpaEntity.create(
                            id,
                            organisationId,
                            ligne.getProduitId(),
                            numeroLot,
                            LocalDate.now().plusYears(10),
                            "ACTIF",
                            "Lot d'ajustement inventaire " + inventaireId,
                            validatedBy,
                            now
                    ));
                    return id;
                });

        StockEmplacementJpaEntity se = stock.findByOrganisationIdAndEmplacementIdAndLotId(organisationId, ligne.getEmplacementId(), lotId)
                .orElseGet(() -> StockEmplacementJpaEntity.create(UUID.randomUUID(), organisationId, ligne.getEmplacementId(), lotId, 0, now));
        se.setQuantite(se.getQuantite() + aAjouter, now);
        stock.save(se);

        mouvements.save(MouvementStockJpaEntity.create(
                new MouvementStockJpaEntity.MouvementInit(
                        UUID.randomUUID(),
                        organisationId,
                        "AJUSTEMENT",
                        lotId,
                        ligne.getProduitId(),
                        aAjouter,
                        null,
                        ligne.getEmplacementId(),
                        "INV-" + inventaireId,
                        ligne.getMotifEcart(),
                        validatedBy,
                        now
                )
        )));
    }
}

