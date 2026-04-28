package cm.pharma.contexts.assurance_mutuelle.application.service;

import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.OrganismeCouvertureJpaEntity;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Calculateur tiers payant (V1) basé sur des heuristiques simples.
 *
 * <p>En pratique, les organismes appliquent des règles complexes; ce service
 * fournit une base paramétrable (taux + catégories) qui s’intègre au POS.</p>
 */
public final class TiersPayantCalculator {
    private TiersPayantCalculator() {
    }

    public static BigDecimal tauxApplicable(OrganismeCouvertureJpaEntity c, ProduitJpaEntity p) {
        if (p.isEstStupefiant() || p.isEstPsychotrope()) {
            return c.getTauxStupefiants();
        }
        String type = p.getTypeProduit() == null ? "" : p.getTypeProduit().toUpperCase(Locale.ROOT);
        if (type.contains("PARA")) {
            return c.getTauxParapharma();
        }
        // Médicaments : tentative de distinction générique/marque
        boolean generique = isGenerique(p);
        return generique ? c.getTauxGenerique() : c.getTauxMarque();
    }

    private static boolean isGenerique(ProduitJpaEntity p) {
        String dci = p.getDci();
        String nom = p.getNomCommercial();
        if (dci == null || dci.isBlank() || nom == null || nom.isBlank()) {
            return false;
        }
        String d = dci.trim().toUpperCase(Locale.ROOT);
        String n = nom.trim().toUpperCase(Locale.ROOT);
        return n.contains(d);
    }

    public static BigDecimal computePriseEnCharge(BigDecimal montantTotal, BigDecimal tauxPct) {
        if (montantTotal == null) {
            montantTotal = BigDecimal.ZERO;
        }
        if (tauxPct == null) {
            tauxPct = BigDecimal.ZERO;
        }
        if (montantTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return montantTotal.multiply(tauxPct).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
    }
}

