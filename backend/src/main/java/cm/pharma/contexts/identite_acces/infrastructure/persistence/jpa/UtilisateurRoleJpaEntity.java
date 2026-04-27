package cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "utilisateur_role")
public class UtilisateurRoleJpaEntity {

    @EmbeddedId
    private UtilisateurRoleId id;

    protected UtilisateurRoleJpaEntity() {
    }

    public static UtilisateurRoleJpaEntity link(UtilisateurRoleId id) {
        UtilisateurRoleJpaEntity e = new UtilisateurRoleJpaEntity();
        e.id = id;
        return e;
    }
}

