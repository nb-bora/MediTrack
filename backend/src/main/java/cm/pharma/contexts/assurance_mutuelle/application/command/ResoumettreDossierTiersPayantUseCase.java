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
public class ResoumettreDossierTiersPayantUseCase {

    private final DossierTiersPayantJpaRepository dossiers;
    private final AlerteService alertes;

    public ResoumettreDossierTiersPayantUseCase(DossierTiersPayantJpaRepository dossiers, AlerteService alertes) {
        this.dossiers = Objects.requireNonNull(dossiers);
        this.alertes = Objects.requireNonNull(alertes);
    }

    @Transactional
    public void execute(UUID organisationId, UUID dossierId, UUID actorId) {
        DossierTiersPayantJpaEntity d = dossiers.findByOrganisationIdAndId(organisationId, dossierId)
                .orElseThrow(() -> new BusinessRuleViolationException("Dossier introuvable"));
        if (!"REJETE".equals(d.getStatut())) {
            throw new BusinessRuleViolationException("Dossier non resoumettable");
        }
        d.resoumettre(actorId, Instant.now());
        dossiers.save(d);

        alertes.openDedup(organisationId, "DOSSIER_TP_RESOUMIS", "INFO", "DossierTiersPayant", dossierId.toString(), "Dossier resoumis", actorId);
    }
}

