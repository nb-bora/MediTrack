package cm.pharma.contexts.stocks_tracabilite.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.EmplacementJpaEntity;
import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.EmplacementJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.LotStockJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.LotStockJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.MouvementStockJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.MouvementStockJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.StockEmplacementJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.StockEmplacementJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SortirLotPourRetourFournisseurUseCase {

    private static final String EMPLACEMENT_QUARANTAINE_CODE = "QUARANTAINE";

    private final LotStockJpaRepository lots;
    private final StockEmplacementJpaRepository stock;
    private final MouvementStockJpaRepository mouvements;
    private final EmplacementJpaRepository emplacements;
    private final AuditWriter auditWriter;

    public SortirLotPourRetourFournisseurUseCase(
            LotStockJpaRepository lots,
            StockEmplacementJpaRepository stock,
            MouvementStockJpaRepository mouvements,
            EmplacementJpaRepository emplacements,
            AuditWriter auditWriter
    ) {
        this.lots = Objects.requireNonNull(lots);
        this.stock = Objects.requireNonNull(stock);
        this.mouvements = Objects.requireNonNull(mouvements);
        this.emplacements = Objects.requireNonNull(emplacements);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public void execute(UUID organisationId, UUID lotId, int quantite, String motif, UUID actorId, String referenceDocument) {
        if (quantite <= 0) {
            throw new BusinessRuleViolationException("Quantité invalide");
        }
        LotStockJpaEntity lot = lots.findById(lotId).orElseThrow(() -> new BusinessRuleViolationException("Lot introuvable"));
        if (!lot.getOrganisationId().equals(organisationId)) {
            throw new BusinessRuleViolationException("Lot hors organisation");
        }

        EmplacementJpaEntity quarantaine = emplacements.findByOrganisationIdAndCode(organisationId, EMPLACEMENT_QUARANTAINE_CODE)
                .orElseThrow(() -> new BusinessRuleViolationException("Emplacement QUARANTAINE introuvable"));

        StockEmplacementJpaEntity se = stock.findByOrganisationIdAndEmplacementIdAndLotId(organisationId, quarantaine.getId(), lotId)
                .orElseThrow(() -> new BusinessRuleViolationException("Stock QUARANTAINE introuvable pour ce lot"));

        if (se.getQuantite() < quantite) {
            throw new BusinessRuleViolationException("Quantité insuffisante en QUARANTAINE");
        }

        Instant now = Instant.now();
        se.setQuantite(se.getQuantite() - quantite, now);
        stock.save(se);

        mouvements.save(MouvementStockJpaEntity.create(new MouvementStockJpaEntity.MouvementInit(
                UUID.randomUUID(),
                organisationId,
                "RETOUR_FOURNISSEUR",
                lotId,
                lot.getProduitId(),
                quantite,
                quarantaine.getId(),
                null,
                referenceDocument,
                motif,
                actorId,
                now
        )));

        // Si tout est sorti, on marque le lot en RETOUR_FOURNISSEUR (sinon on laisse l’état actuel).
        int remaining = stock.findByOrganisationIdAndLotId(organisationId, lotId).stream().mapToInt(StockEmplacementJpaEntity::getQuantite).sum();
        if (remaining == 0) {
            lot.setStatut("RETOUR_FOURNISSEUR", motif, actorId, now);
        }

        auditWriter.write(AuditEvent.simple(
                organisationId, now, null, null, null,
                null, null, "RETOUR_FOURNISSEUR_SORTIE_STOCK", "LotStock", lotId.toString(), motif,
                Map.of("produit_id", lot.getProduitId(), "quantite", quantite, "reference_document", referenceDocument)
        ));
    }
}

