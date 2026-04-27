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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MettreLotEnQuarantaineUseCase {

    private static final String EMPLACEMENT_QUARANTAINE_CODE = "QUARANTAINE";
    private static final String LOT_STATUT_QUARANTAINE = "QUARANTAINE";

    private final LotStockJpaRepository lots;
    private final StockEmplacementJpaRepository stock;
    private final MouvementStockJpaRepository mouvements;
    private final EmplacementJpaRepository emplacements;
    private final AuditWriter auditWriter;

    public MettreLotEnQuarantaineUseCase(
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
    public void execute(UUID organisationId, UUID lotId, String motif, UUID actorId) {
        LotStockJpaEntity lot = lots.findById(lotId)
                .orElseThrow(() -> new BusinessRuleViolationException("Lot introuvable"));
        if (!lot.getOrganisationId().equals(organisationId)) {
            throw new BusinessRuleViolationException("Lot hors organisation");
        }

        EmplacementJpaEntity quarantaine = emplacements.findByOrganisationIdAndCode(organisationId, EMPLACEMENT_QUARANTAINE_CODE)
                .orElseThrow(() -> new BusinessRuleViolationException("Emplacement QUARANTAINE introuvable"));

        Instant now = Instant.now();
        lot.setStatut(LOT_STATUT_QUARANTAINE, motif, actorId, now);

        List<StockEmplacementJpaEntity> rows = stock.findByOrganisationIdAndLotId(organisationId, lotId);
        for (StockEmplacementJpaEntity row : rows) {
            boolean shouldMove = row.getQuantite() > 0 && !row.getEmplacementId().equals(quarantaine.getId());
            if (shouldMove) {
                int qty = row.getQuantite();
                row.setQuantite(0, now);
                stock.save(row);

                StockEmplacementJpaEntity dst = stock.findByOrganisationIdAndEmplacementIdAndLotId(organisationId, quarantaine.getId(), lotId)
                        .orElseGet(() -> StockEmplacementJpaEntity.create(UUID.randomUUID(), organisationId, quarantaine.getId(), lotId, 0, now));
                dst.setQuantite(dst.getQuantite() + qty, now);
                stock.save(dst);

                mouvements.save(MouvementStockJpaEntity.create(
                        new MouvementStockJpaEntity.MouvementInit(
                                UUID.randomUUID(),
                                organisationId,
                                "MISE_QUARANTAINE",
                                lotId,
                                lot.getProduitId(),
                                qty,
                                row.getEmplacementId(),
                                quarantaine.getId(),
                                null,
                                motif,
                                actorId,
                                now
                        )
                )));
            }
        }

        auditWriter.write(AuditEvent.simple(
                organisationId, now, null, null, null,
                null, null, "LOT_MIS_EN_QUARANTAINE", "LotStock", lotId.toString(), motif,
                Map.of("produit_id", lot.getProduitId())
        ));
    }
}

