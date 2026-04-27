package cm.pharma.contexts.achats_fournisseurs.bootstrap;

import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.BonCommandeJpaRepository;
import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.OrganisationJpaRepository;
import cm.pharma.shared.application.AlerteService;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BonsCommandeRetardScheduler {

    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final OrganisationJpaRepository organisations;
    private final BonCommandeJpaRepository bons;
    private final AlerteService alertes;

    public BonsCommandeRetardScheduler(OrganisationJpaRepository organisations, BonCommandeJpaRepository bons, AlerteService alertes) {
        this.organisations = Objects.requireNonNull(organisations);
        this.bons = Objects.requireNonNull(bons);
        this.alertes = Objects.requireNonNull(alertes);
    }

    @Scheduled(cron = "0 15 7 * * *")
    public void runEveryMorning() {
        LocalDate today = LocalDate.now();
        organisations.findAll().forEach(org ->
            bons.findByOrganisationIdOrderByCreatedAtDesc(org.getId()).stream()
                    .filter(b -> b.getDateLivraisonPrevue() != null)
                    .filter(b -> java.util.Set.of("VALIDE", "RECU_PARTIEL").contains(b.getStatut()))
                    .filter(b -> b.getDateLivraisonPrevue().isBefore(today))
                    .forEach(b -> alertes.openDedup(
                            org.getId(),
                            "BON_COMMANDE_EN_RETARD",
                            "IMPORTANT",
                            "BonCommande",
                            b.getId().toString(),
                            "Bon de commande en retard: " + b.getNumero(),
                            SYSTEM_USER_ID
                    )));
    }
}

