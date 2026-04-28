package cm.pharma.contexts.stocks_tracabilite.bootstrap;

import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.OrganisationJpaRepository;
import java.util.Objects;
import java.util.UUID;
import cm.pharma.contexts.stocks_tracabilite.application.command.MettreAJourStatutsPeremptionLotsUseCase;
import cm.pharma.contexts.referentiel.application.service.ParametresService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LotsPerimesScheduler {

    /**
     * Utilisateur "système" pour l’audit des traitements automatiques.
     *
     * <p>V1 : on utilise un UUID constant (documenté) sans FK.
     * Les champs {@code created_by/updated_by} sont nullable, donc on ne bloque pas en DB.
     * </p>
     */
    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final OrganisationJpaRepository organisations;
    private final MettreAJourStatutsPeremptionLotsUseCase majStatutsPeremption;
    private final ParametresService parametres;

    public LotsPerimesScheduler(
            OrganisationJpaRepository organisations,
            MettreAJourStatutsPeremptionLotsUseCase majStatutsPeremption,
            ParametresService parametres
    ) {
        this.organisations = Objects.requireNonNull(organisations);
        this.majStatutsPeremption = Objects.requireNonNull(majStatutsPeremption);
        this.parametres = Objects.requireNonNull(parametres);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void runEveryMidnight() {
        organisations.findAll().forEach(org -> {
            int precoce = parametres.getInt(org.getId(), "ALERTE_PEREMPTION_PRECOCE_JOURS", 90);
            int urgente = parametres.getInt(org.getId(), "ALERTE_PEREMPTION_URGENTE_JOURS", 30);
            majStatutsPeremption.execute(org.getId(), precoce, urgente, SYSTEM_USER_ID);
        });
    }
}

