package cm.pharma.contexts.stocks_tracabilite.bootstrap;

import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.OrganisationJpaRepository;
import java.util.Objects;
import java.util.UUID;
import cm.pharma.contexts.stocks_tracabilite.application.command.MettreAJourStatutsPeremptionLotsUseCase;
import org.springframework.beans.factory.annotation.Value;
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
    private final int precoceJours;
    private final int urgenteJours;

    public LotsPerimesScheduler(
            OrganisationJpaRepository organisations,
            MettreAJourStatutsPeremptionLotsUseCase majStatutsPeremption,
            @Value("${pharma.stocks.alertes.peremption.precoce_jours:90}") int precoceJours,
            @Value("${pharma.stocks.alertes.peremption.urgente_jours:30}") int urgenteJours
    ) {
        this.organisations = Objects.requireNonNull(organisations);
        this.majStatutsPeremption = Objects.requireNonNull(majStatutsPeremption);
        this.precoceJours = precoceJours;
        this.urgenteJours = urgenteJours;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void runEveryMidnight() {
        organisations.findAll().forEach(org -> majStatutsPeremption.execute(org.getId(), precoceJours, urgenteJours, SYSTEM_USER_ID));
    }
}

