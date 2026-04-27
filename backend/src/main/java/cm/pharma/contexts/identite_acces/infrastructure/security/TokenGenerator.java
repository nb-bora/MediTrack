package cm.pharma.contexts.identite_acces.infrastructure.security;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

/**
 * Générateur de secrets (mots de passe temporaires, refresh tokens).
 */
@Component
public class TokenGenerator {

    private final SecureRandom random = new SecureRandom();

    public String randomUrlSafeToken(int bytes) {
        byte[] buf = new byte[bytes];
        random.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}

