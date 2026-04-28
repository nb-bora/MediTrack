package cm.pharma.contexts.stocks_tracabilite.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.LotStockJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.LotStockJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.RappelLotJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.RappelLotJpaRepository;
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
public class CreerRappelLotUseCase {

    private final LotStockJpaRepository lots;
    private final StockEmplacementJpaRepository stock;
    private final RappelLotJpaRepository rappels;
    private final MettreLotEnQuarantaineUseCase mettreEnQuarantaine;
    private final AuditWriter auditWriter;

    public CreerRappelLotUseCase(
            LotStockJpaRepository lots,
            StockEmplacementJpaRepository stock,
            RappelLotJpaRepository rappels,
            MettreLotEnQuarantaineUseCase mettreEnQuarantaine,
            AuditWriter auditWriter
    ) {
        this.lots = Objects.requireNonNull(lots);
        this.stock = Objects.requireNonNull(stock);
        this.rappels = Objects.requireNonNull(rappels);
        this.mettreEnQuarantaine = Objects.requireNonNull(mettreEnQuarantaine);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    /**
     * Crée un rappel fournisseur et bloque le lot immédiatement.
     */
    @Transactional
    public UUID execute(CreerRappelLotCommand cmd) {
        Objects.requireNonNull(cmd);
        LotStockJpaEntity lot = lots.findById(cmd.lotId())
                .orElseThrow(() -> new BusinessRuleViolationException("Lot introuvable"));
        if (!lot.getOrganisationId().equals(cmd.organisationId())) {
            throw new BusinessRuleViolationException("Lot hors organisation");
        }
        rappels.findByOrganisationIdAndLotId(cmd.organisationId(), cmd.lotId())
                .ifPresent(r -> {
                    throw new BusinessRuleViolationException("Rappel déjà existant pour ce lot");
                });

        Instant now = Instant.now();
        lot.setStatut("RAPPELE", "Rappel fournisseur: " + cmd.motif(), cmd.creePar(), now);

        UUID rappelId = UUID.randomUUID();
        rappels.save(RappelLotJpaEntity.create(
                rappelId,
                cmd.organisationId(),
                lot.getProduitId(),
                lot.getId(),
                cmd.criticite().trim(),
                cmd.motif().trim(),
                cmd.source(),
                cmd.creePar(),
                now
        ));

        // Mise en quarantaine physique (déplacement vers l’emplacement QUARANTAINE) + mouvements.
        mettreEnQuarantaine.execute(cmd.organisationId(), lot.getId(), "RAPPEL FOURNISSEUR", cmd.creePar());
        List<StockEmplacementJpaEntity> rows = stock.findDisponiblesParLot(cmd.organisationId(), lot.getId());
        int total = rows.stream().mapToInt(StockEmplacementJpaEntity::getQuantite).sum();

        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, null, null, null,
                null, null, "RAPPEL_LOT_CREE", "LotStock", lot.getId().toString(), cmd.motif(),
                Map.of(
                        "rappel_id", rappelId,
                        "produit_id", lot.getProduitId(),
                        "criticite", cmd.criticite(),
                        "source", cmd.source(),
                        "quantite_localisee", total
                )
        ));

        return rappelId;
    }
}

