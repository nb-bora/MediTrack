package cm.pharma.contexts.identite_acces.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.identite_acces.domain.service.PasswordPolicy;
import cm.pharma.contexts.identite_acces.infrastructure.security.PasswordHasher;
import cm.pharma.contexts.identite_acces.infrastructure.security.TokenGenerator;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.RoleJpaEntity;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.RoleJpaRepository;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurJpaEntity;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurJpaRepository;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurRoleId;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurRoleJpaEntity;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurRoleJpaRepository;
import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.OrganisationJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cas d’usage : crée le premier administrateur (bootstrap).
 */
@Service
public class CreerAdminInitialUseCase {

    private final OrganisationJpaRepository organisations;
    private final UtilisateurJpaRepository utilisateurs;
    private final RoleJpaRepository roles;
    private final UtilisateurRoleJpaRepository utilisateurRoles;
    private final PasswordHasher hasher;
    private final TokenGenerator tokenGenerator;
    private final AuditWriter auditWriter;

    public CreerAdminInitialUseCase(
            OrganisationJpaRepository organisations,
            UtilisateurJpaRepository utilisateurs,
            RoleJpaRepository roles,
            UtilisateurRoleJpaRepository utilisateurRoles,
            PasswordHasher hasher,
            TokenGenerator tokenGenerator,
            AuditWriter auditWriter
    ) {
        this.organisations = Objects.requireNonNull(organisations);
        this.utilisateurs = Objects.requireNonNull(utilisateurs);
        this.roles = Objects.requireNonNull(roles);
        this.utilisateurRoles = Objects.requireNonNull(utilisateurRoles);
        this.hasher = Objects.requireNonNull(hasher);
        this.tokenGenerator = Objects.requireNonNull(tokenGenerator);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public CreerAdminInitialResult execute(CreerAdminInitialCommand cmd) {
        Objects.requireNonNull(cmd, "cmd est requis");

        if (organisations.count() == 0) {
            throw new BusinessRuleViolationException("Setup requis : initialiser l’organisation avant de créer un utilisateur");
        }

        if (utilisateurs.count() > 0) {
            throw new BusinessRuleViolationException("Un utilisateur existe déjà : bootstrap admin interdit");
        }

        UUID organisationId = organisations.findFirstByOrderByCreatedAtAsc()
                .map(o -> o.getId())
                .orElse(null);
        if (organisationId == null) {
            throw new BusinessRuleViolationException("Organisation introuvable");
        }

        // Mot de passe temporaire : on le renvoie UNE SEULE fois au client.
        String tempPassword = tokenGenerator.randomUrlSafeToken(12) + "A1!";
        PasswordPolicy.assertValid(tempPassword);

        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(90, ChronoUnit.DAYS);

        utilisateurs.save(UtilisateurJpaEntity.create(
                userId,
                organisationId,
                cmd.nom(),
                cmd.prenom(),
                cmd.email(),
                cmd.telephone(),
                cmd.login(),
                hasher.hash(tempPassword),
                expiresAt,
                now
        ));

        // Crée le rôle ADMIN dans cette organisation si absent, puis l’assigne.
        UUID roleAdminId = ensureRoleExists(organisationId, "ADMIN", "Administrateur", "Administrateur système", now);
        utilisateurRoles.save(UtilisateurRoleJpaEntity.link(new UtilisateurRoleId(userId, roleAdminId)));

        // Seed minimal des autres rôles si absents (utiles pour la suite).
        ensureRoleExists(organisationId, "PHARMACIEN", "Pharmacien", "Pharmacien titulaire/responsable", now);
        ensureRoleExists(organisationId, "CAISSIER", "Caissier", "Caissier / assistant pharmacien", now);
        ensureRoleExists(organisationId, "MAGASINIER", "Magasinier", "Gestionnaire de stock", now);
        ensureRoleExists(organisationId, "COMPTABLE", "Comptable", "Comptable / finance", now);

        auditWriter.write(AuditEvent.simple(
                organisationId, now, userId, cmd.prenom() + " " + cmd.nom(), "ADMIN",
                null, null, "UTILISATEUR_CREE", "Utilisateur", userId.toString(), null,
                Map.of("login", cmd.login(), "type", "ADMIN_INITIAL")
        ));

        return new CreerAdminInitialResult(userId, organisationId, tempPassword);
    }

    private UUID ensureRoleExists(UUID organisationId, String code, String nom, String description, Instant now) {
        return roles.findByOrganisationIdAndCode(organisationId, code)
                .map(RoleJpaEntity::getId)
                .orElseGet(() -> {
                    UUID id = UUID.randomUUID();
                    roles.save(RoleJpaEntity.create(id, organisationId, code, nom, description, now));
                    return id;
                });
    }

    public record CreerAdminInitialResult(UUID utilisateurId, UUID organisationId, String motDePasseTemporaire) {
    }
}

