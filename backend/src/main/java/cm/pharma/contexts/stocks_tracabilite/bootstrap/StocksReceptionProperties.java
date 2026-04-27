package cm.pharma.contexts.stocks_tracabilite.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pharma.stocks.reception")
public class StocksReceptionProperties {

    /**
     * Si la péremption est plus proche que ce seuil, une confirmation pharmacien est requise.
     */
    private int alertePeremptionMois = 6;

    private final ChaineFroid chaineFroid = new ChaineFroid();

    public int getAlertePeremptionMois() {
        return alertePeremptionMois;
    }

    public void setAlertePeremptionMois(int alertePeremptionMois) {
        this.alertePeremptionMois = alertePeremptionMois;
    }

    public ChaineFroid getChaineFroid() {
        return chaineFroid;
    }

    public static class ChaineFroid {
        private double tempMinC = 2.0;
        private double tempMaxC = 8.0;

        public double getTempMinC() {
            return tempMinC;
        }

        public void setTempMinC(double tempMinC) {
            this.tempMinC = tempMinC;
        }

        public double getTempMaxC() {
            return tempMaxC;
        }

        public void setTempMaxC(double tempMaxC) {
            this.tempMaxC = tempMaxC;
        }
    }
}

