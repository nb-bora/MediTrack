package cm.pharma.contexts.ordonnances_dossier_patient.bootstrap;

import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.OrganisationJpaRepository;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceJpaRepository;
import cm.pharma.shared.application.AlerteService;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrdonnancesRenouvellementScheduler {

    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final OrganisationJpaRepository organisations;
    private final OrdonnanceJpaRepository ordonnances;
    private final AlerteService alertes;

    public OrdonnancesRenouvellementScheduler(OrganisationJpaRepository organisations, OrdonnanceJpaRepository ordonnances, AlerteService alertes) {
        this.organisations = Objects.requireNonNull(organisations);
        this.ordonnances = Objects.requireNonNull(ordonnances);
        this.alertes = Objects.requireNonNull(alertes);
    }

    @Scheduled(cron = "0 15 0 * * *") // 00:15 chaque jour
    public void run() {
        LocalDate j7 = LocalDate.now().plusDays(7);
        organisations.findAll().forEach(org -> ordonnances.findAlerteRenouvellementJ7(org.getId(), j7).forEach(o ->
            alertes.openDedup(
                    org.getId(),
                    "ORDONNANCE_EXPIRE_BIENTOT",
                    "IMPORTANT",
                    "Ordonnance",
                    o.getId().toString(),
                    "Ordonnance expire dans 7 jours — prévoir renouvellement",
                    SYSTEM_USER_ID
            );
        ));
    }
}

