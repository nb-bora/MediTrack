package cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class UtilisateurRoleId implements Serializable {

    @Column(name = "utilisateur_id", nullable = false)
    private UUID utilisateurId;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    protected UtilisateurRoleId() {
    }

    public UtilisateurRoleId(UUID utilisateurId, UUID roleId) {
        this.utilisateurId = utilisateurId;
        this.roleId = roleId;
    }
}

