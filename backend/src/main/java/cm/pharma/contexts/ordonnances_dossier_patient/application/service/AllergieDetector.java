package cm.pharma.contexts.ordonnances_dossier_patient.application.service;

import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaEntity;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

/**
 * Détection d’allergies “mode réel” (V1).
 *
 * <p>Objectif: éviter les faux négatifs sur les allergies critiques, tout en restant
 * simple tant qu’on n’a pas un référentiel thérapeutique complet.</p>
 */
public final class AllergieDetector {
    private AllergieDetector() {
    }

    private static final Set<String> ALLERGIE_PENICILLINES_SYNONYMES = Set.of(
            "PENICILLINE", "PENICILLINES", "PENICILLIN", "PENICILLINS", "PENICILL"
    );

    private static final Set<String> DCI_PENICILLINES = Set.of(
            "AMOXICILLINE", "AMPICILLINE", "BENZYLPENICILLINE", "PENICILLINE"
    );

    public static boolean isBlocant(String allergiesRaw, ProduitJpaEntity produit) {
        if (allergiesRaw == null || allergiesRaw.isBlank() || produit == null) {
            return false;
        }
        return isBlocant(allergiesRaw, produit.getNomCommercial(), produit.getDci());
    }

    public static boolean isBlocant(String allergiesRaw, String nomCommercial, String dci) {
        if (allergiesRaw == null || allergiesRaw.isBlank()) {
            return false;
        }
        String allergies = normalize(allergiesRaw);
        String nom = normalize(nomCommercial);
        String dciN = normalize(dci);

        boolean allergiePenicillines = containsAny(allergies, ALLERGIE_PENICILLINES_SYNONYMES);
        boolean produitPenicilline = containsAny(dciN, DCI_PENICILLINES)
                || nom.contains("AMOXICIL") || dciN.contains("AMOXICIL")
                || nom.contains("PENICILL") || dciN.contains("PENICILL");

        return allergiePenicillines && produitPenicilline;
    }

    private static boolean containsAny(String haystack, Set<String> needles) {
        if (haystack == null || haystack.isBlank()) {
            return false;
        }
        for (String n : needles) {
            if (haystack.contains(n)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String s) {
        if (s == null) {
            return "";
        }
        String upper = s.toUpperCase(Locale.ROOT);
        String decomposed = Normalizer.normalize(upper, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}+", "");
    }
}

