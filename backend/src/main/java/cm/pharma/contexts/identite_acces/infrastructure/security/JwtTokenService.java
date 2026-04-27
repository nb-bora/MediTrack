package cm.pharma.contexts.identite_acces.infrastructure.security;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/**
 * Service de génération des access tokens JWT.
 *
 * <p>On place les rôles dans une claim {@code roles} pour que l’API puisse appliquer RBAC.</p>
 */
@Component
public class JwtTokenService {

    private final JwtEncoder encoder;
    private final String issuer;
    private final Duration accessTtl;

    public JwtTokenService(
            JwtEncoder encoder,
            @Value("${pharma.security.jwt.issuer}") String issuer,
            @Value("${pharma.security.jwt.access-token-ttl}") Duration accessTtl
    ) {
        this.encoder = Objects.requireNonNull(encoder);
        this.issuer = Objects.requireNonNull(issuer);
        this.accessTtl = Objects.requireNonNull(accessTtl);
    }

    public String createAccessToken(String subjectUserId, String organisationId, List<String> roles, String posteNom) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(accessTtl))
                .subject(subjectUserId)
                .claim("organisation_id", organisationId)
                .claim("poste_nom", posteNom)
                .claim("roles", roles == null ? List.of() : roles)
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}

