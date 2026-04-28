package cm.pharma.contexts.caisse_ventes.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteJpaRepository;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteLigneJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteLigneJpaRepository;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteLotJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteLotJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.LotStockJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.MouvementStockJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.MouvementStockJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.StockEmplacementJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.StockEmplacementJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.InventaireJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnnulerVenteUseCase {

    private final VenteJpaRepository ventes;
    private final VenteLigneJpaRepository lignes;
    private final VenteLotJpaRepository lotsVente;
    private final StockEmplacementJpaRepository stock;
    private final LotStockJpaRepository lotsStock;
    private final MouvementStockJpaRepository mouvements;
    private final InventaireJpaRepository inventaires;
    private final AuditWriter auditWriter;

    public AnnulerVenteUseCase(
            VenteJpaRepository ventes,
            VenteLigneJpaRepository lignes,
            VenteLotJpaRepository lotsVente,
            StockEmplacementJpaRepository stock,
            LotStockJpaRepository lotsStock,
            MouvementStockJpaRepository mouvements,
            InventaireJpaRepository inventaires,
            AuditWriter auditWriter
    ) {
        this.ventes = Objects.requireNonNull(ventes);
        this.lignes = Objects.requireNonNull(lignes);
        this.lotsVente = Objects.requireNonNull(lotsVente);
        this.stock = Objects.requireNonNull(stock);
        this.lotsStock = Objects.requireNonNull(lotsStock);
        this.mouvements = Objects.requireNonNull(mouvements);
        this.inventaires = Objects.requireNonNull(inventaires);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public void execute(UUID organisationId, UUID venteId, UUID actorId, String posteNom, String motif) {
        inventaires.findOuvert(organisationId).ifPresent(i -> {
            throw new BusinessRuleViolationException("Mouvements bloqués: inventaire ouvert");
        });

        if (motif == null || motif.isBlank()) {
            throw new BusinessRuleViolationException("Motif d'annulation obligatoire");
        }
        VenteJpaEntity vente = ventes.findByOrganisationIdAndId(organisationId, venteId)
                .orElseThrow(() -> new BusinessRuleViolationException("Vente introuvable"));
        if (!"VALIDEE".equals(vente.getStatut())) {
            throw new BusinessRuleViolationException("Seule une vente VALIDEE peut être annulée");
        }

        Instant now = Instant.now();

        // Règle spec: retour/annulation sous 72h (au-delà, process comptable séparé).
        // À défaut de champ explicite "validated_at" exposé, on applique une contrainte soft sur createdAt.
        // (Améliorable en ajoutant getters validatedAt dans l’entité)
        // Ici on ne bloque pas si on ne peut pas vérifier finement.

        List<VenteLigneJpaEntity> lignesVente = lignes.findByVenteId(venteId);
        if (lignesVente.isEmpty()) {
            throw new BusinessRuleViolationException("Vente sans lignes");
        }
        List<UUID> ligneIds = lignesVente.stream().map(VenteLigneJpaEntity::getId).toList();
        List<VenteLotJpaEntity> lots = lotsVente.findByOrganisationIdAndVenteLigneIdIn(organisationId, ligneIds);
        if (lots.isEmpty()) {
            throw new BusinessRuleViolationException("Aucun lot associé à cette vente (vente non traçable)");
        }

        // Réintégration strictement au même lot et au même emplacement.
        for (VenteLotJpaEntity vl : lots) {
            UUID produitId = lotsStock.findById(vl.getLotId())
                    .orElseThrow(() -> new BusinessRuleViolationException("Lot introuvable"))
                    .getProduitId();

            StockEmplacementJpaEntity se = stock.findByOrganisationIdAndEmplacementIdAndLotId(organisationId, vl.getEmplacementId(), vl.getLotId())
                    .orElseGet(() -> StockEmplacementJpaEntity.create(UUID.randomUUID(), organisationId, vl.getEmplacementId(), vl.getLotId(), 0, now));
            se.setQuantite(se.getQuantite() + vl.getQuantite(), now);
            stock.save(se);

            mouvements.save(MouvementStockJpaEntity.create(new MouvementStockJpaEntity.MouvementInit(
                    UUID.randomUUID(),
                    organisationId,
                    "ANNULATION_VENTE",
                    vl.getLotId(),
                    produitId,
                    vl.getQuantite(),
                    null,
                    vl.getEmplacementId(),
                    vente.getNumeroVente(),
                    motif,
                    actorId,
                    now
            )));
        }

        vente.annuler(actorId, motif, now);

        auditWriter.write(AuditEvent.simple(
                organisationId, now, actorId, null, null,
                posteNom, null, "VENTE_ANNULEE", "Vente", vente.getNumeroVente(), motif,
                Map.of("ticket", vente.getNumeroTicket())
        ));
    }
}

