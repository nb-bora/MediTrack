package cm.pharma.contexts.identite_acces.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Component;

/**
 * Hachage des refresh tokens.
 *
 * <p>On ne stocke jamais un refresh token en clair en base. En V1, on utilise SHA-256
 * (suffisant pour un secret aléatoire de haute entropie).</p>
 */
@Component
public class RefreshTokenHasher {

    public String sha256(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit((b) & 0xF, 16));
        }
        return sb.toString();
    }
}

