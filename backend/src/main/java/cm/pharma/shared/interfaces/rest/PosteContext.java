package cm.pharma.shared.interfaces.rest;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Extraction du poste (caisse) depuis le JWT.
 *
 * <p>Contrat attendu (claim) :
 * <ul>
 *   <li>{@code poste_nom}: String (ex: {@code "CAISSE-01"})</li>
 * </ul>
 * </p>
 */
public final class PosteContext {
    private static final String CLAIM_POSTE_NOM = "poste_nom";

    private PosteContext() {
    }

    public static String posteNom(JwtAuthenticationToken auth) {
        Jwt jwt = auth.getToken();
        String raw = jwt.getClaimAsString(CLAIM_POSTE_NOM);
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("JWT sans claim poste_nom");
        }
        return raw.trim();
    }
}

