package cm.pharma.contexts.identite_acces.infrastructure.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rate limiting simple sur l’endpoint de login (anti brute force).
 *
 * <p>Décision V1 : in-memory (suffisant en mono-poste / petit LAN). Si besoin, on migrera
 * vers une implémentation distribuée (Redis) en V2.</p>
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final String DEFAULT_LOGIN_PATH = "/api/auth/login";
    private static final int CAPACITY = 10;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final String loginPath;

    public LoginRateLimitFilter(@Value("${pharma.security.ratelimit.login-path:" + DEFAULT_LOGIN_PATH + "}") String loginPath) {
        this.loginPath = loginPath == null || loginPath.isBlank() ? DEFAULT_LOGIN_PATH : loginPath;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !loginPath.equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = key(request);
        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket());
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Trop de tentatives, réessayer plus tard\"}");
    }

    private static Bucket newBucket() {
        Refill refill = Refill.intervally(CAPACITY, REFILL_PERIOD);
        Bandwidth limit = Bandwidth.classic(CAPACITY, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private static String key(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

