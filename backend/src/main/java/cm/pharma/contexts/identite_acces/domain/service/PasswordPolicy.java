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
        assertValid(rawPassword, MIN_LENGTH, true, true, true);
    }

    public static void assertValid(String rawPassword, int minLength, boolean requireUpper, boolean requireDigit, boolean requireSpecial) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new BusinessRuleViolationException("Mot de passe requis");
        }
        int ml = Math.max(1, minLength);
        if (rawPassword.length() < ml) {
            throw new BusinessRuleViolationException("Mot de passe trop court (minimum " + ml + " caractères)");
        }
        if (requireUpper && !UPPER.matcher(rawPassword).matches()) {
            throw new BusinessRuleViolationException("Mot de passe invalide : une majuscule est requise");
        }
        if (requireDigit && !DIGIT.matcher(rawPassword).matches()) {
            throw new BusinessRuleViolationException("Mot de passe invalide : un chiffre est requis");
        }
        if (requireSpecial && !SPECIAL.matcher(rawPassword).matches()) {
            throw new BusinessRuleViolationException("Mot de passe invalide : un caractère spécial est requis");
        }
    }
}

