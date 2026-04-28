package cm.pharma.contexts.assurance_mutuelle.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pharma.stockage.dossiers-tiers-payant")
public record TiersPayantStockageProperties(String baseDir) {
}

