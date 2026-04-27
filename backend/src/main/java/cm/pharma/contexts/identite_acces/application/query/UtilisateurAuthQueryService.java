package cm.pharma.contexts.identite_acces.application.query;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurJpaEntity;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurJpaRepository;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurRoleJpaRepository;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurRoleId;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurRoleJpaEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

/**
 * Service de requêtes JPA pour l’authentification.
 */
@Service
public class UtilisateurAuthQueryService {

    private final UtilisateurJpaRepository utilisateurs;
    private final UtilisateurRolesQuery rolesQuery;

    public UtilisateurAuthQueryService(UtilisateurJpaRepository utilisateurs, UtilisateurRolesQuery rolesQuery) {
        this.utilisateurs = utilisateurs;
        this.rolesQuery = rolesQuery;
    }

    public UtilisateurAuthView findByLogin(String login) {
        UtilisateurJpaEntity u = utilisateurs.findByLogin(login).orElse(null);
        if (u == null) {
            return null;
        }
        List<String> roles = rolesQuery.findRoleCodesByUtilisateurId(u.getId());

        return new UtilisateurAuthView(
                u.getId(),
                u.getOrganisationId(),
                u.getNom(),
                u.getPrenom(),
                u.getLogin(),
                u.getPasswordHash(),
                u.isActif(),
                u.isDoitChangerMdp(),
                u.getMdpExpiresAt(),
                u.getTentativesEchec(),
                u.getVerrouilleJusqua(),
                roles == null ? List.of() : List.copyOf(roles)
        );
    }
}

