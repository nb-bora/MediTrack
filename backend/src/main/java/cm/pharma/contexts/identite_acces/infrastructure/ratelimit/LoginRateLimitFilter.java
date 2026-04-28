package cm.pharma.contexts.identite_acces.infrastructure.ratelimit;

import cm.pharma.contexts.identite_acces.application.query.UtilisateurAuthQueryService;
import cm.pharma.contexts.referentiel.application.service.ParametresService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * Rate limiting simple sur l’endpoint de login (anti brute force).
 *
 * <p>Décision V1 : in-memory (suffisant en mono-poste / petit LAN). Si besoin, on migrera
 * vers une implémentation distribuée (Redis) en V2.</p>
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final String DEFAULT_LOGIN_PATH = "/api/auth/login";
    private static final int DEFAULT_CAPACITY = 10;
    private static final Duration DEFAULT_REFILL_PERIOD = Duration.ofMinutes(1);

    private final Map<String, BucketWithConfig> buckets = new ConcurrentHashMap<>();
    private final String loginPath;
    private final UtilisateurAuthQueryService authQuery;
    private final ParametresService parametres;

    public LoginRateLimitFilter(
            @Value("${pharma.security.ratelimit.login-path:" + DEFAULT_LOGIN_PATH + "}") String loginPath,
            UtilisateurAuthQueryService authQuery,
            ParametresService parametres
    ) {
        this.loginPath = loginPath == null || loginPath.isBlank() ? DEFAULT_LOGIN_PATH : loginPath;
        this.authQuery = Objects.requireNonNull(authQuery);
        this.parametres = Objects.requireNonNull(parametres);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !loginPath.equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingRequestWrapper wrapped = request instanceof ContentCachingRequestWrapper c ? c : new ContentCachingRequestWrapper(request, 4096);

        UUID orgId = organisationIdFromLogin(wrapped);
        boolean enabled = orgId == null || parametres.getBoolean(orgId, "LOGIN_RATELIMIT_ENABLED", true);
        if (!enabled) {
            filterChain.doFilter(wrapped, response);
            return;
        }

        int capacity = orgId == null ? DEFAULT_CAPACITY : Math.max(1, parametres.getInt(orgId, "LOGIN_RATELIMIT_CAPACITY", DEFAULT_CAPACITY));
        int refillSeconds = orgId == null ? (int) DEFAULT_REFILL_PERIOD.toSeconds() : Math.max(1, parametres.getInt(orgId, "LOGIN_RATELIMIT_REFILL_SECONDS", (int) DEFAULT_REFILL_PERIOD.toSeconds()));
        Duration refillPeriod = Duration.ofSeconds(refillSeconds);

        String key = key(wrapped, orgId);
        Bucket bucket = buckets.compute(key, (k, existing) -> {
            if (existing == null || existing.capacity != capacity || !existing.refillPeriod.equals(refillPeriod) || existing.expiresAt.isBefore(Instant.now())) {
                return new BucketWithConfig(newBucket(capacity, refillPeriod), capacity, refillPeriod, Instant.now().plus(Duration.ofMinutes(10)));
            }
            return existing;
        }).bucket;

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(wrapped, response);
            return;
        }
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Trop de tentatives, réessayer plus tard\"}");
    }

    private static Bucket newBucket(int capacity, Duration refillPeriod) {
        Refill refill = Refill.intervally(capacity, refillPeriod);
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private static String key(HttpServletRequest request, UUID organisationId) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        String org = organisationId == null ? "UNKNOWN" : organisationId.toString();
        return org + "|" + ip;
    }

    private UUID organisationIdFromLogin(ContentCachingRequestWrapper request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return null;
        }
        String login = extractLogin(readBodyAsString(request));
        if (login == null || login.isBlank()) {
            return null;
        }
        var view = authQuery.findByLogin(login);
        return view == null ? null : view.organisationId();
    }

    private static String readBodyAsString(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content == null || content.length == 0) {
            // Force la lecture pour remplir le cache si besoin.
            try {
                content = request.getInputStream().readAllBytes();
            } catch (Exception e) {
                return null;
            }
        }
        if (content == null || content.length == 0) {
            return null;
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    private static String extractLogin(String body) {
        if (body == null) {
            return null;
        }
        // Extraction volontairement simple (sans dépendre d’un parseur JSON) : recherche du champ "login".
        String b = body.toLowerCase(Locale.ROOT);
        int i = b.indexOf("\"login\"");
        if (i < 0) {
            return null;
        }
        int colon = b.indexOf(':', i);
        if (colon < 0) {
            return null;
        }
        int firstQuote = body.indexOf('"', colon + 1);
        if (firstQuote < 0) {
            return null;
        }
        int secondQuote = body.indexOf('"', firstQuote + 1);
        if (secondQuote < 0) {
            return null;
        }
        return body.substring(firstQuote + 1, secondQuote).trim();
    }

    private record BucketWithConfig(Bucket bucket, int capacity, Duration refillPeriod, Instant expiresAt) {
    }
}

