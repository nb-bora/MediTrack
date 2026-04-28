package cm.pharma.contexts.referentiel.application.service;

import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.ParametreJpaEntity;
import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.ParametreJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service de lecture/écriture de paramètres métiers par organisation.
 *
 * <p>Règle : si un paramètre n'existe pas en base, on retourne la valeur par défaut (comportement actuel du code).</p>
 */
@Service
public class ParametresService {

    public static final String TYPE_STRING = "STRING";
    public static final String TYPE_NUMBER = "NUMBER";
    public static final String TYPE_BOOL = "BOOL";
    public static final String TYPE_DURATION = "DURATION";

    private final ParametreJpaRepository repo;

    private final ConcurrentHashMap<String, CachedValue> cache = new ConcurrentHashMap<>();
    private final Duration ttl = Duration.ofSeconds(30);

    public ParametresService(ParametreJpaRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    @Transactional(readOnly = true)
    public String getString(UUID organisationId, String cle, String defaultValue) {
        return getRaw(organisationId, cle).orElse(defaultValue);
    }

    @Transactional(readOnly = true)
    public int getInt(UUID organisationId, String cle, int defaultValue) {
        String v = getRaw(organisationId, cle).orElse(null);
        if (v == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Paramètre invalide (int) : " + cle);
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal getBigDecimal(UUID organisationId, String cle, BigDecimal defaultValue) {
        String v = getRaw(organisationId, cle).orElse(null);
        if (v == null) {
            return defaultValue;
        }
        try {
            return new BigDecimal(v.trim());
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Paramètre invalide (decimal) : " + cle);
        }
    }

    @Transactional(readOnly = true)
    public boolean getBoolean(UUID organisationId, String cle, boolean defaultValue) {
        String v = getRaw(organisationId, cle).orElse(null);
        if (v == null) {
            return defaultValue;
        }
        String t = v.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(t) || "1".equals(t) || "oui".equals(t) || "yes".equals(t)) {
            return true;
        }
        if ("false".equals(t) || "0".equals(t) || "non".equals(t) || "no".equals(t)) {
            return false;
        }
        throw new BusinessRuleViolationException("Paramètre invalide (bool) : " + cle);
    }

    @Transactional(readOnly = true)
    public Duration getDuration(UUID organisationId, String cle, Duration defaultValue) {
        String v = getRaw(organisationId, cle).orElse(null);
        if (v == null) {
            return defaultValue;
        }
        try {
            // ISO-8601 : PT15M, PT8H, P1D...
            return Duration.parse(v.trim());
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Paramètre invalide (duration ISO-8601) : " + cle);
        }
    }

    @Transactional
    public UUID upsert(UUID organisationId, String cle, String valeur, String typeValeur, String description) {
        Objects.requireNonNull(organisationId);
        if (cle == null || cle.isBlank()) {
            throw new BusinessRuleViolationException("Clé paramètre requise");
        }
        if (valeur == null) {
            throw new BusinessRuleViolationException("Valeur paramètre requise");
        }
        String normalizedKey = cle.trim().toUpperCase(Locale.ROOT);
        String normalizedType = typeValeur == null ? TYPE_STRING : typeValeur.trim().toUpperCase(Locale.ROOT);

        Instant now = Instant.now();
        ParametreJpaEntity p = repo.findByOrganisationIdAndCle(organisationId, normalizedKey).orElse(null);
        if (p == null) {
            UUID id = UUID.randomUUID();
            repo.save(ParametreJpaEntity.create(id, organisationId, normalizedKey, valeur.trim(), normalizedType, description, now));
            invalidate(organisationId, normalizedKey);
            return id;
        }
        p.update(valeur.trim(), normalizedType, description, now);
        invalidate(organisationId, normalizedKey);
        return p.getId();
    }

    private void invalidate(UUID organisationId, String cle) {
        cache.remove(cacheKey(organisationId, cle));
    }

    private Optional<String> getRaw(UUID organisationId, String cle) {
        if (organisationId == null) {
            throw new BusinessRuleViolationException("organisationId requis");
        }
        if (cle == null || cle.isBlank()) {
            throw new BusinessRuleViolationException("Clé paramètre requise");
        }
        String normalizedKey = cle.trim().toUpperCase(Locale.ROOT);
        String key = cacheKey(organisationId, normalizedKey);
        CachedValue cached = cache.get(key);
        Instant now = Instant.now();
        if (cached != null && cached.expiresAt.isAfter(now)) {
            return Optional.ofNullable(cached.value);
        }
        String value = repo.findByOrganisationIdAndCle(organisationId, normalizedKey)
                .map(ParametreJpaEntity::getValeur)
                .orElse(null);
        cache.put(key, new CachedValue(value, now.plus(ttl)));
        return Optional.ofNullable(value);
    }

    private static String cacheKey(UUID organisationId, String cle) {
        return organisationId + "::" + cle;
    }

    private record CachedValue(String value, Instant expiresAt) {
    }
}

