package cm.pharma.contexts.ordonnances_dossier_patient.bootstrap;

import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.OrganisationJpaRepository;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceJpaRepository;
import cm.pharma.contexts.referentiel.application.service.ParametresService;
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
    private final ParametresService parametres;

    public OrdonnancesRenouvellementScheduler(
            OrganisationJpaRepository organisations,
            OrdonnanceJpaRepository ordonnances,
            AlerteService alertes,
            ParametresService parametres
    ) {
        this.organisations = Objects.requireNonNull(organisations);
        this.ordonnances = Objects.requireNonNull(ordonnances);
        this.alertes = Objects.requireNonNull(alertes);
        this.parametres = Objects.requireNonNull(parametres);
    }

    @Scheduled(cron = "0 15 0 * * *") // 00:15 chaque jour
    public void run() {
        LocalDate today = LocalDate.now();
        organisations.findAll().forEach(org -> {
            int jours = parametres.getInt(org.getId(), "ORDONNANCE_ALERTE_RENOUVELLEMENT_JOURS", 7);
            LocalDate cible = today.plusDays(jours);
            ordonnances.findAlerteRenouvellementJ7(org.getId(), cible).forEach(o ->
                alertes.openDedup(
                        org.getId(),
                        "ORDONNANCE_EXPIRE_BIENTOT",
                        "IMPORTANT",
                        "Ordonnance",
                        o.getId().toString(),
                        "Ordonnance expire dans " + jours + " jours — prévoir renouvellement",
                        SYSTEM_USER_ID
                )
            );
        });
    }
}

