package cm.pharma.bootstrap;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import cm.pharma.contexts.identite_acces.infrastructure.ratelimit.LoginRateLimitFilter;

/**
 * Configuration de sécurité (V1).
 *
 * <p>Objectifs documentés :
 * <ul>
 *   <li>JWT (stateless) avec durée courte (access token)</li>
 *   <li>refresh token stocké côté serveur (table {@code session_auth})</li>
 *   <li>HTTPS local (auto-signé) : sera configuré via propriétés/certificat</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter,
            LoginRateLimitFilter loginRateLimitFilter
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/api-docs/**", "/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/setup/initialiser").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/setup/creer-admin-initial").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }

    @Bean
    JwtEncoder jwtEncoder(@Value("${pharma.security.jwt.hmac-secret}") String secret) {
        SecretKey key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        return NimbusJwtEncoder.withSecretKey(key).build();
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${pharma.security.jwt.hmac-secret}") String secret) {
        SecretKey key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}

