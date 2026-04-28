package cm.pharma.contexts.assurance_mutuelle.application.command;

import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.DossierTiersPayantJpaEntity;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.DossierTiersPayantJpaRepository;
import cm.pharma.shared.application.AlerteService;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RejeterDossierTiersPayantUseCase {

    private final DossierTiersPayantJpaRepository dossiers;
    private final AlerteService alertes;

    public RejeterDossierTiersPayantUseCase(DossierTiersPayantJpaRepository dossiers, AlerteService alertes) {
        this.dossiers = Objects.requireNonNull(dossiers);
        this.alertes = Objects.requireNonNull(alertes);
    }

    @Transactional
    public void execute(UUID organisationId, UUID dossierId, String motif, UUID actorId) {
        if (motif == null || motif.isBlank()) {
            throw new BusinessRuleViolationException("Motif de rejet requis");
        }
        DossierTiersPayantJpaEntity d = dossiers.findByOrganisationIdAndId(organisationId, dossierId)
                .orElseThrow(() -> new BusinessRuleViolationException("Dossier introuvable"));
        if (!"SOUMIS".equals(d.getStatut()) && !"RESOUMIS".equals(d.getStatut())) {
            throw new BusinessRuleViolationException("Dossier non rejetable");
        }
        d.rejeter(motif, actorId, Instant.now());
        dossiers.save(d);

        alertes.openDedup(
                organisationId,
                "DOSSIER_TP_REJETE",
                "IMPORTANT",
                "DossierTiersPayant",
                dossierId.toString(),
                "Dossier rejeté: " + motif,
                actorId
        );
    }
}

