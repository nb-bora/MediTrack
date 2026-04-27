package cm.pharma.contexts.ordonnances_dossier_patient.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pharma.stockage.ordonnances")
public record OrdonnancesStockageProperties(String baseDir) {
}

