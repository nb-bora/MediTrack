package cm.pharma.contexts.identite_acces.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.identite_acces.domain.service.PasswordPolicy;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurJpaEntity;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurJpaRepository;
import cm.pharma.contexts.identite_acces.infrastructure.security.PasswordHasher;
import cm.pharma.contexts.identite_acces.infrastructure.security.TokenGenerator;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminGererUtilisateurUseCase {

    private final UtilisateurJpaRepository utilisateurs;
    private final PasswordHasher hasher;
    private final TokenGenerator tokenGenerator;
    private final AuditWriter auditWriter;

    public AdminGererUtilisateurUseCase(UtilisateurJpaRepository utilisateurs, PasswordHasher hasher, TokenGenerator tokenGenerator, AuditWriter auditWriter) {
        this.utilisateurs = Objects.requireNonNull(utilisateurs);
        this.hasher = Objects.requireNonNull(hasher);
        this.tokenGenerator = Objects.requireNonNull(tokenGenerator);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public void setActif(UUID organisationId, UUID utilisateurId, boolean actif, UUID actorId, String posteNom, String ip) {
        UtilisateurJpaEntity u = utilisateurs.findByOrganisationIdAndId(organisationId, utilisateurId)
                .orElseThrow(() -> new BusinessRuleViolationException("Utilisateur introuvable"));
        u.setActif(actif);
        u.touch(Instant.now());
        utilisateurs.save(u);

        auditWriter.write(AuditEvent.simple(
                organisationId, Instant.now(), actorId, null, "ADMIN",
                posteNom, ip, actif ? "UTILISATEUR_ACTIVE" : "UTILISATEUR_DESACTIVE",
                "Utilisateur", utilisateurId.toString(), null, Map.of()
        ));
    }

    @Transactional
    public void deverrouiller(UUID organisationId, UUID utilisateurId, UUID actorId, String posteNom, String ip) {
        UtilisateurJpaEntity u = utilisateurs.findByOrganisationIdAndId(organisationId, utilisateurId)
                .orElseThrow(() -> new BusinessRuleViolationException("Utilisateur introuvable"));
        u.resetLockout();
        u.touch(Instant.now());
        utilisateurs.save(u);

        auditWriter.write(AuditEvent.simple(
                organisationId, Instant.now(), actorId, null, "ADMIN",
                posteNom, ip, "COMPTE_DEVERROUILLE",
                "Utilisateur", utilisateurId.toString(), null, Map.of()
        ));
    }

    @Transactional
    public ResetMdpResult resetMotDePasse(UUID organisationId, UUID utilisateurId, UUID actorId, String posteNom, String ip) {
        UtilisateurJpaEntity u = utilisateurs.findByOrganisationIdAndId(organisationId, utilisateurId)
                .orElseThrow(() -> new BusinessRuleViolationException("Utilisateur introuvable"));

        String tempPassword = tokenGenerator.randomUrlSafeToken(12) + "A1!";
        PasswordPolicy.assertValid(tempPassword);

        Instant now = Instant.now();
        Instant expiresAt = now.plus(90, ChronoUnit.DAYS);
        u.setPasswordHash(hasher.hash(tempPassword));
        u.setDoitChangerMdp(true);
        u.setMdpExpiresAt(expiresAt);
        u.resetLockout();
        u.touch(now);
        utilisateurs.save(u);

        auditWriter.write(AuditEvent.simple(
                organisationId, now, actorId, null, "ADMIN",
                posteNom, ip, "MDP_RESET_ADMIN",
                "Utilisateur", utilisateurId.toString(), null, Map.of()
        ));

        return new ResetMdpResult(tempPassword);
    }

    public record ResetMdpResult(String motDePasseTemporaire) {
    }
}

