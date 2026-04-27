package cm.pharma;

import cm.pharma.contexts.ordonnances_dossier_patient.bootstrap.OrdonnancesStockageProperties;
import cm.pharma.contexts.stocks_tracabilite.bootstrap.StocksReceptionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Point d’entrée de l’API du système de gestion de pharmacie.
 *
 * <p>Règle d’architecture : ce module assemble l’application (composition root) via Spring Boot,
 * mais les règles métier doivent rester dans les packages {@code shared.domain} et
 * {@code contexts.*.domain} sans dépendance à Spring/JPA/Web.</p>
 */
@SpringBootApplication
@EnableMethodSecurity
@EnableScheduling
@EnableConfigurationProperties({StocksReceptionProperties.class, OrdonnancesStockageProperties.class})
public class PharmaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PharmaApplication.class, args);
    }
}

