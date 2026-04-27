package cm.pharma.bootstrap;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/**
 * Conversion JWT -> authorities Spring Security.
 *
 * <p>On convertit la claim {@code roles} en autorités {@code ROLE_*}.</p>
 */
@Configuration
public class JwtAuthConverterConfig {

    @Bean
    Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
            Collection<GrantedAuthority> authorities = defaultConverter.convert(jwt);

            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                authorities.addAll(
                        roles.stream()
                                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                                .collect(Collectors.toSet())
                );
            }
            return new JwtAuthenticationToken(jwt, authorities);
        };
    }
}

