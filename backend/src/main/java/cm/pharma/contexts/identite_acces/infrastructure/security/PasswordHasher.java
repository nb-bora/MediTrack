package cm.pharma.contexts.identite_acces.infrastructure.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Service de hachage des mots de passe.
 *
 * <p>Exigence : BCrypt (jamais en clair).</p>
 */
@Component
public class PasswordHasher {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String passwordHash) {
        return encoder.matches(rawPassword, passwordHash);
    }
}

