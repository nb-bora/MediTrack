package cm.pharma.contexts.identite_acces.application.query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Vue applicative pour l’authentification (projection SQL).
 */
public record UtilisateurAuthView(
        UUID utilisateurId,
        UUID organisationId,
        String nom,
        String prenom,
        String login,
        String passwordHash,
        boolean actif,
        boolean doitChangerMdp,
        Instant mdpExpiresAt,
        int tentativesEchec,
        Instant verrouilleJusqua,
        List<String> roles
) {
    public String fullName() {
        return prenom + " " + nom;
    }
}

