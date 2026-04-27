package cm.pharma.shared.interfaces.rest;

import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Extraction des informations multi-tenant depuis le JWT.
 *
 * <p>Contrat attendu (claim) :
 * <ul>
 *   <li>{@code organisation_id}: UUID</li>
 * </ul>
 * </p>
 */
public final class OrganisationContext {
    private static final String CLAIM_ORGANISATION_ID = "organisation_id";

    private OrganisationContext() {
    }

    public static UUID organisationId(JwtAuthenticationToken auth) {
        Jwt jwt = auth.getToken();
        String raw = jwt.getClaimAsString(CLAIM_ORGANISATION_ID);
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("JWT sans claim organisation_id");
        }
        return UUID.fromString(raw);
    }
}

package cm.pharma.shared.interfaces.rest;

import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.util.UUID;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Extraction “sans ambiguïté” du contexte d’organisation depuis le JWT.
 *
 * <p>Règle : l’API ne doit pas faire confiance à un {@code organisationId} fourni par le client
 * si l’utilisateur est authentifié. L’organisation est dérivée du token.</p>
 */
public final class OrganisationContext {

    private OrganisationContext() {
    }

    public static UUID organisationId(JwtAuthenticationToken auth) {
        if (auth == null) {
            throw new BusinessRuleViolationException("Authentification requise");
        }
        String org = auth.getToken().getClaimAsString("organisation_id");
        if (org == null || org.isBlank()) {
            throw new BusinessRuleViolationException("Token invalide : organisation_id manquant");
        }
        return UUID.fromString(org);
    }
}

