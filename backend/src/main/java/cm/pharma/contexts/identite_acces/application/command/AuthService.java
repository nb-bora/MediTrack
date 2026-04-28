package cm.pharma.contexts.identite_acces.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.identite_acces.application.query.UtilisateurAuthQueryService;
import cm.pharma.contexts.identite_acces.application.query.UtilisateurAuthView;
import cm.pharma.contexts.identite_acces.domain.service.PasswordPolicy;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.PasswordHistoryJpaEntity;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.PasswordHistoryJpaRepository;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.SessionAuthJpaEntity;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.SessionAuthJpaRepository;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurJpaEntity;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurJpaRepository;
import cm.pharma.contexts.identite_acces.infrastructure.security.JwtTokenService;
import cm.pharma.contexts.identite_acces.infrastructure.security.PasswordHasher;
import cm.pharma.contexts.identite_acces.infrastructure.security.RefreshTokenHasher;
import cm.pharma.contexts.identite_acces.infrastructure.security.TokenGenerator;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service applicatif d’authentification (login/refresh/logout).
 *
 * <p>Ce service respecte les exigences Module B :
 * verrouillage après 5 échecs, expiration MDP, refresh token stocké hashé, audit.</p>
 */
@Service
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_UNTIL_FAR_FUTURE = Duration.ofDays(3650); // “débloqué uniquement par admin”
    private static final String ENTITY_UTILISATEUR = "Utilisateur";
    private static final String ENTITY_SESSION_AUTH = "SessionAuth";

    private final UtilisateurAuthQueryService authQuery;
    private final PasswordHasher passwordHasher;
    private final JwtTokenService jwtTokenService;
    private final TokenGenerator tokenGenerator;
    private final RefreshTokenHasher refreshTokenHasher;
    private final SessionAuthJpaRepository sessions;
    private final UtilisateurJpaRepository utilisateurs;
    private final PasswordHistoryJpaRepository passwordHistory;
    private final AuditWriter auditWriter;
    private final Duration refreshTtl;

    public AuthService(
            UtilisateurAuthQueryService authQuery,
            PasswordHasher passwordHasher,
            JwtTokenService jwtTokenService,
            TokenGenerator tokenGenerator,
            RefreshTokenHasher refreshTokenHasher,
            SessionAuthJpaRepository sessions,
            UtilisateurJpaRepository utilisateurs,
            PasswordHistoryJpaRepository passwordHistory,
            AuditWriter auditWriter,
            @Value("${pharma.security.jwt.refresh-token-ttl}") Duration refreshTtl
    ) {
        this.authQuery = Objects.requireNonNull(authQuery);
        this.passwordHasher = Objects.requireNonNull(passwordHasher);
        this.jwtTokenService = Objects.requireNonNull(jwtTokenService);
        this.tokenGenerator = Objects.requireNonNull(tokenGenerator);
        this.refreshTokenHasher = Objects.requireNonNull(refreshTokenHasher);
        this.sessions = Objects.requireNonNull(sessions);
        this.utilisateurs = Objects.requireNonNull(utilisateurs);
        this.passwordHistory = Objects.requireNonNull(passwordHistory);
        this.auditWriter = Objects.requireNonNull(auditWriter);
        this.refreshTtl = Objects.requireNonNull(refreshTtl);
    }

    @Transactional
    public LoginResult login(String login, String rawPassword, String poste, String ip) {
        if (poste == null || poste.isBlank()) {
            throw new BusinessRuleViolationException("Poste requis (en-tête X-Poste)");
        }
        if (login == null || login.isBlank()) {
            throw new BusinessRuleViolationException("Login requis");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new BusinessRuleViolationException("Mot de passe requis");
        }

        UtilisateurAuthView user = authQuery.findByLogin(login);
        if (user == null) {
            // Ne pas révéler si le login existe.
            throw new BusinessRuleViolationException("Identifiants invalides");
        }
        if (!user.actif()) {
            throw new BusinessRuleViolationException("Compte désactivé");
        }
        Instant now = Instant.now();
        if (user.verrouilleJusqua() != null && user.verrouilleJusqua().isAfter(now)) {
            throw new BusinessRuleViolationException("Compte verrouillé (déblocage administrateur requis)");
        }
        if (user.mdpExpiresAt() != null && user.mdpExpiresAt().isBefore(now)) {
            throw new BusinessRuleViolationException("Mot de passe expiré (changement requis)");
        }

        boolean ok = passwordHasher.matches(rawPassword, user.passwordHash());
        if (!ok) {
            registerFailedAttempt(user.utilisateurId(), user.organisationId(), user.fullName(), pickRole(user), poste, ip);
            throw new BusinessRuleViolationException("Identifiants invalides");
        }

        resetFailedAttempts(user.utilisateurId());

        String accessToken = jwtTokenService.createAccessToken(
                user.utilisateurId().toString(),
                user.organisationId().toString(),
                user.roles(),
                poste
        );

        String refreshToken = tokenGenerator.randomUrlSafeToken(48);
        String refreshHash = refreshTokenHasher.sha256(refreshToken);
        Instant expireLe = now.plus(refreshTtl);
        UUID sessionId = UUID.randomUUID();

        sessions.save(SessionAuthJpaEntity.create(sessionId, user.utilisateurId(), now, expireLe, refreshHash, poste, ip));

        auditWriter.write(AuditEvent.simple(
                user.organisationId(), now, user.utilisateurId(), user.fullName(), pickRole(user),
                poste, ip, "CONNEXION_REUSSIE", ENTITY_UTILISATEUR, user.utilisateurId().toString(), null,
                Map.of("login", user.login())
        ));

        return new LoginResult(accessToken, refreshToken, user.doitChangerMdp());
    }

    public record LoginResult(String accessToken, String refreshToken, boolean doitChangerMdp) {
    }

    @Transactional
    public RefreshResult refresh(String refreshToken, String poste, String ip) {
        if (poste == null || poste.isBlank()) {
            throw new BusinessRuleViolationException("Poste requis (en-tête X-Poste)");
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessRuleViolationException("Refresh token requis");
        }
        String hash = refreshTokenHasher.sha256(refreshToken);
        Instant now = Instant.now();

        SessionAuthJpaEntity session = sessions.findFirstByRefreshTokenHash(hash).orElse(null);
        if (session == null) {
            throw new BusinessRuleViolationException("Refresh token invalide");
        }
        if (session.getRevokeeLe() != null) {
            throw new BusinessRuleViolationException("Session révoquée");
        }
        if (session.getExpireLe().isBefore(now)) {
            throw new BusinessRuleViolationException("Session expirée");
        }

        // Rotation refresh token (sécurité)
        String newRefresh = tokenGenerator.randomUrlSafeToken(48);
        String newHash = refreshTokenHasher.sha256(newRefresh);
        Instant newExpire = now.plus(refreshTtl);

        session.rotate(newHash, newExpire, poste, ip);
        sessions.save(session);

        // Roles
        UUID userId = session.getUtilisateurId();
        UtilisateurJpaEntity u = utilisateurs.findById(userId).orElse(null);
        if (u == null) {
            throw new BusinessRuleViolationException("Session invalide (utilisateur introuvable)");
        }
        UtilisateurAuthView user = authQuery.findByLogin(u.getLogin());
        var roles = user == null ? java.util.List.<String>of() : user.roles();

        String access = jwtTokenService.createAccessToken(
                userId.toString(),
                u.getOrganisationId().toString(),
                roles,
                poste
        );

        auditWriter.write(AuditEvent.simple(
                u.getOrganisationId(), now, userId, null, null,
                poste, ip, "TOKEN_REFRESH", ENTITY_SESSION_AUTH, session.getId().toString(), null,
                Map.of()
        ));

        return new RefreshResult(access, newRefresh);
    }

    public record RefreshResult(String accessToken, String refreshToken) {
    }

    @Transactional
    public void logout(String refreshToken, String poste, String ip) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        Instant now = Instant.now();
        String hash = refreshTokenHasher.sha256(refreshToken);

        SessionAuthJpaEntity session = sessions.findFirstByRefreshTokenHash(hash).orElse(null);
        if (session == null) {
            return;
        }

        session.revoke(now);
        sessions.save(session);

        UUID userId = session.getUtilisateurId();
        UtilisateurJpaEntity u = utilisateurs.findById(userId).orElse(null);
        if (u == null) {
            return;
        }
        auditWriter.write(AuditEvent.simple(
                u.getOrganisationId(), now, userId, null, null,
                poste, ip, "DECONNEXION", ENTITY_SESSION_AUTH, session.getId().toString(), null,
                Map.of()
        ));
    }

    @Transactional
    public void changerMotDePasse(UUID utilisateurId, String ancienMdp, String nouveauMdp) {
        Objects.requireNonNull(utilisateurId, "utilisateurId requis");
        PasswordPolicy.assertValid(nouveauMdp);

        UtilisateurJpaEntity user = utilisateurs.findById(utilisateurId).orElse(null);
        if (user == null) {
            throw new BusinessRuleViolationException("Utilisateur introuvable");
        }
        if (!passwordHasher.matches(ancienMdp, user.getPasswordHash())) {
            throw new BusinessRuleViolationException("Ancien mot de passe incorrect");
        }

        // Vérifie non-réutilisation des 5 derniers
        var history = passwordHistory.findTop5ByUtilisateurIdOrderByCreatedAtDesc(utilisateurId);
        for (var h : history) {
            if (passwordHasher.matches(nouveauMdp, h.getPasswordHash())) {
                throw new BusinessRuleViolationException("Mot de passe déjà utilisé récemment (5 derniers)");
            }
        }
        // inclut l’actuel
        if (passwordHasher.matches(nouveauMdp, user.getPasswordHash())) {
            throw new BusinessRuleViolationException("Mot de passe identique à l’actuel");
        }

        String newHash = passwordHasher.hash(nouveauMdp);
        Instant now = Instant.now();
        Instant expiresAt = now.plus(90, ChronoUnit.DAYS);

        // Archive l’ancien hash dans l’historique
        passwordHistory.save(PasswordHistoryJpaEntity.from(UUID.randomUUID(), utilisateurId, user.getPasswordHash(), now));

        user.setPasswordHash(newHash);
        user.setDoitChangerMdp(false);
        user.setMdpExpiresAt(expiresAt);
        user.touch(now);
        utilisateurs.save(user);

        auditWriter.write(AuditEvent.simple(
                user.getOrganisationId(), now, utilisateurId, null, null,
                null, null, "MDP_CHANGE", ENTITY_UTILISATEUR, utilisateurId.toString(), null,
                Map.of()
        ));
    }

    private void registerFailedAttempt(UUID userId, UUID orgId, String userName, String role, String poste, String ip) {
        Instant now = Instant.now();
        UtilisateurJpaEntity user = utilisateurs.findById(userId).orElse(null);
        if (user != null) {
            int attempts = user.getTentativesEchec() + 1;
            user.setTentativesEchec(attempts);
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setVerrouilleJusqua(now.plus(LOCK_UNTIL_FAR_FUTURE));
            }
            user.touch(now);
            utilisateurs.save(user);

            if (attempts == MAX_FAILED_ATTEMPTS) {
                auditWriter.write(AuditEvent.simple(
                        user.getOrganisationId(), now, userId, user.getPrenom() + " " + user.getNom(), null,
                        poste, ip, "COMPTE_VERROUILLE", ENTITY_UTILISATEUR, userId.toString(), null,
                        Map.of("tentatives_echec", attempts)
                ));
            }
        }

        auditWriter.write(AuditEvent.simple(
                orgId, now, userId, userName, role,
                poste, ip, "CONNEXION_ECHEC", ENTITY_UTILISATEUR, userId.toString(), null,
                Map.of("tentatives_echec", user == null ? 0 : user.getTentativesEchec())
        ));
    }

    private void resetFailedAttempts(UUID userId) {
        UtilisateurJpaEntity user = utilisateurs.findById(userId).orElse(null);
        if (user != null) {
            user.setTentativesEchec(0);
            user.setVerrouilleJusqua(null);
            user.touch(Instant.now());
            utilisateurs.save(user);
        }
    }

    private static String pickRole(UtilisateurAuthView user) {
        return user.roles() == null || user.roles().isEmpty() ? null : user.roles().get(0);
    }

}

