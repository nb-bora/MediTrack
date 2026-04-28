package cm.pharma.contexts.identite_acces.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.identite_acces.domain.service.PasswordPolicy;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.RoleJpaEntity;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.RoleJpaRepository;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurJpaEntity;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurJpaRepository;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurRoleId;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurRoleJpaEntity;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurRoleJpaRepository;
import cm.pharma.contexts.identite_acces.infrastructure.security.PasswordHasher;
import cm.pharma.contexts.identite_acces.infrastructure.security.TokenGenerator;
import cm.pharma.contexts.referentiel.application.service.ParametresService;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreerUtilisateurUseCase {

    private final UtilisateurJpaRepository utilisateurs;
    private final RoleJpaRepository roles;
    private final UtilisateurRoleJpaRepository utilisateurRoles;
    private final PasswordHasher hasher;
    private final TokenGenerator tokenGenerator;
    private final AuditWriter auditWriter;
    private final ParametresService parametres;

    public CreerUtilisateurUseCase(
            UtilisateurJpaRepository utilisateurs,
            RoleJpaRepository roles,
            UtilisateurRoleJpaRepository utilisateurRoles,
            PasswordHasher hasher,
            TokenGenerator tokenGenerator,
            AuditWriter auditWriter,
            ParametresService parametres
    ) {
        this.utilisateurs = Objects.requireNonNull(utilisateurs);
        this.roles = Objects.requireNonNull(roles);
        this.utilisateurRoles = Objects.requireNonNull(utilisateurRoles);
        this.hasher = Objects.requireNonNull(hasher);
        this.tokenGenerator = Objects.requireNonNull(tokenGenerator);
        this.auditWriter = Objects.requireNonNull(auditWriter);
        this.parametres = Objects.requireNonNull(parametres);
    }

    @Transactional
    public CreerUtilisateurResult execute(CreerUtilisateurCommand cmd) {
        Objects.requireNonNull(cmd);
        if (cmd.login() == null || cmd.login().isBlank()) {
            throw new BusinessRuleViolationException("Login requis");
        }
        if (cmd.nom() == null || cmd.nom().isBlank() || cmd.prenom() == null || cmd.prenom().isBlank()) {
            throw new BusinessRuleViolationException("Nom et prénom requis");
        }
        if (cmd.roleCode() == null || cmd.roleCode().isBlank()) {
            throw new BusinessRuleViolationException("Rôle requis");
        }
        String login = cmd.login().trim();
        if (utilisateurs.findByOrganisationIdAndLogin(cmd.organisationId(), login).isPresent()) {
            throw new BusinessRuleViolationException("Login déjà utilisé");
        }

        RoleJpaEntity role = roles.findByOrganisationIdAndCode(cmd.organisationId(), cmd.roleCode().trim().toUpperCase())
                .orElseThrow(() -> new BusinessRuleViolationException("Rôle introuvable"));

        // Mot de passe temporaire : renvoyé UNE SEULE fois
        String tempPassword = tokenGenerator.randomUrlSafeToken(12) + "A1!";
        int minLen = parametres.getInt(cmd.organisationId(), "PASSWORD_MIN_LENGTH", 8);
        boolean reqUpper = parametres.getBoolean(cmd.organisationId(), "PASSWORD_REQUIRE_UPPER", true);
        boolean reqDigit = parametres.getBoolean(cmd.organisationId(), "PASSWORD_REQUIRE_DIGIT", true);
        boolean reqSpecial = parametres.getBoolean(cmd.organisationId(), "PASSWORD_REQUIRE_SPECIAL", true);
        PasswordPolicy.assertValid(tempPassword, minLen, reqUpper, reqDigit, reqSpecial);

        Instant now = Instant.now();
        int expiryDays = Math.max(1, parametres.getInt(cmd.organisationId(), "PASSWORD_EXPIRY_DAYS", 90));
        Instant expiresAt = now.plus(expiryDays, ChronoUnit.DAYS);
        UUID userId = UUID.randomUUID();

        utilisateurs.save(UtilisateurJpaEntity.create(
                userId,
                cmd.organisationId(),
                cmd.nom().trim(),
                cmd.prenom().trim(),
                cmd.email(),
                cmd.telephone(),
                login,
                hasher.hash(tempPassword),
                expiresAt,
                now
        ));

        utilisateurRoles.save(UtilisateurRoleJpaEntity.link(new UtilisateurRoleId(userId, role.getId())));

        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, cmd.actorId(), null, "ADMIN",
                cmd.posteNom(), cmd.ip(), "UTILISATEUR_CREE", "Utilisateur", userId.toString(), null,
                Map.of("login", login, "role", role.getCode())
        ));

        return new CreerUtilisateurResult(userId, tempPassword);
    }

    public record CreerUtilisateurCommand(
            UUID organisationId,
            String nom,
            String prenom,
            String login,
            String email,
            String telephone,
            String roleCode,
            UUID actorId,
            String posteNom,
            String ip
    ) {
    }

    public record CreerUtilisateurResult(UUID utilisateurId, String motDePasseTemporaire) {
    }
}

