package cm.pharma.contexts.assurance_mutuelle.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pharma.stockage.dossiers_tiers_payant")
public record TiersPayantStockageProperties(String baseDir) {
}

