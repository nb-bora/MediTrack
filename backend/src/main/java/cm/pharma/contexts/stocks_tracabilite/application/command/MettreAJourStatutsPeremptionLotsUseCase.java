package cm.pharma.contexts.stocks_tracabilite.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.LotStockJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.LotStockJpaRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MettreAJourStatutsPeremptionLotsUseCase {

    private final LotStockJpaRepository lots;
    private final AuditWriter auditWriter;

    public MettreAJourStatutsPeremptionLotsUseCase(LotStockJpaRepository lots, AuditWriter auditWriter) {
        this.lots = Objects.requireNonNull(lots);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    /**
     * Met à jour les statuts de péremption selon la proximité de la date :
     *
     * <ul>
     *   <li>si {@code datePeremption < today} → {@code PERIME_AUTOMATIQUE}</li>
     *   <li>si {@code 0 <= joursRestants <= urgenteJours} → {@code PEREMPTION_URGENTE}</li>
     * </ul>
     *
     * <p>La mise à jour ne s’applique qu’aux lots en statut {@code ACTIF} ou {@code PEREMPTION_URGENTE},
     * afin de ne pas écraser {@code QUARANTAINE}/{@code RAPPELE}/etc.</p>
     */
    @Transactional
    public int execute(UUID organisationId, int precoceJours, int urgenteJours, UUID systemUserId) {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(precoceJours);
        List<LotStockJpaEntity> candidats = lots.findLotsAReevaluerPeremption(organisationId, threshold);

        Instant now = Instant.now();
        int changed = 0;
        for (LotStockJpaEntity lot : candidats) {
            String before = lot.getStatut();
            long joursRestants = ChronoUnit.DAYS.between(today, lot.getDatePeremption());

            String after;
            if (lot.getDatePeremption().isBefore(today)) {
                after = "PERIME_AUTOMATIQUE";
            } else if (joursRestants <= urgenteJours) {
                after = "PEREMPTION_URGENTE";
            } else if (joursRestants <= precoceJours) {
                after = "PEREMPTION_PRECOCE";
            } else {
                after = "ACTIF";
            }

            if (!Objects.equals(before, after)) {
                lot.setStatut(after, "MAJ automatique statuts péremption", systemUserId, now);
                changed++;

                auditWriter.write(AuditEvent.simple(
                        organisationId, now, null, null, null,
                        null, null, "LOT_STATUT_PEREMPTION_MAJ", "LotStock", lot.getId().toString(), null,
                        Map.of(
                                "produit_id", lot.getProduitId(),
                                "numero_lot", lot.getNumeroLot(),
                                "date_peremption", lot.getDatePeremption(),
                                "jours_restants", joursRestants,
                                "statut_avant", before,
                                "statut_apres", after
                        )
                ));
            }
        }

        return changed;
    }
}

