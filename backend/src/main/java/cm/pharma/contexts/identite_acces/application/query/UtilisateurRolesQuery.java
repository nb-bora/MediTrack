package cm.pharma.contexts.identite_acces.application.query;

import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurRoleId;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurRoleJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

/**
 * Requête dédiée : récupère les codes rôles d’un utilisateur.
 *
 * <p>On sépare cette requête pour garder la logique d’authentification simple
 * et ne pas complexifier les entités avec des mappings ManyToMany prématurés.</p>
 */
public interface UtilisateurRolesQuery extends Repository<UtilisateurRoleJpaEntity, UtilisateurRoleId> {

    @Query("""
            select r.code
            from cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurRoleJpaEntity ur
            join cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.RoleJpaEntity r
              on r.id = ur.id.roleId
            where ur.id.utilisateurId = :utilisateurId
            """)
    List<String> findRoleCodesByUtilisateurId(UUID utilisateurId);
}

