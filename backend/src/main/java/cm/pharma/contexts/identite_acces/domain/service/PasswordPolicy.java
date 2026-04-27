package cm.pharma.contexts.identite_acces.domain.service;

import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.util.regex.Pattern;

/**
 * Politique de mot de passe (Module B).
 *
 * <p>Exigences :
 * <ul>
 *   <li>Minimum 8 caractères</li>
 *   <li>Au moins 1 majuscule, 1 chiffre, 1 caractère spécial</li>
 * </ul>
 * </p>
 */
public final class PasswordPolicy {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPER = Pattern.compile(".*[A-Z].*");
    private static final Pattern DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL = Pattern.compile(".*[^a-zA-Z0-9].*");

    private PasswordPolicy() {
    }

    public static void assertValid(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new BusinessRuleViolationException("Mot de passe requis");
        }
        if (rawPassword.length() < MIN_LENGTH) {
            throw new BusinessRuleViolationException("Mot de passe trop court (minimum " + MIN_LENGTH + " caractères)");
        }
        if (!UPPER.matcher(rawPassword).matches()) {
            throw new BusinessRuleViolationException("Mot de passe invalide : une majuscule est requise");
        }
        if (!DIGIT.matcher(rawPassword).matches()) {
            throw new BusinessRuleViolationException("Mot de passe invalide : un chiffre est requis");
        }
        if (!SPECIAL.matcher(rawPassword).matches()) {
            throw new BusinessRuleViolationException("Mot de passe invalide : un caractère spécial est requis");
        }
    }
}

