package cm.pharma.shared.interfaces.rest;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.util.UUID;

/**
 * Extraction “sans ambiguïté” du contexte d’organisation depuis le JWT.
 *
 * <p>Règle : l’API ne doit pas faire confiance à un {@code organisationId} fourni par le client
 * si l’utilisateur est authentifié. L’organisation est dérivée du token.</p>
 */
public final class OrganisationContext {
    private static final String CLAIM_ORGANISATION_ID = "organisation_id";

    private OrganisationContext() {
    }

    public static UUID organisationId(JwtAuthenticationToken auth) {
        if (auth == null) {
            throw new BusinessRuleViolationException("Authentification requise");
        }
        String raw = auth.getToken().getClaimAsString(CLAIM_ORGANISATION_ID);
        if (raw == null || raw.isBlank()) {
            throw new BusinessRuleViolationException("Token invalide : organisation_id manquant");
        }
        return UUID.fromString(raw);
    }
}
