package cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "role")
public class RoleJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "description")
    private String description;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RoleJpaEntity() {
    }

    public static RoleJpaEntity create(UUID id, UUID organisationId, String code, String nom, String description, Instant now) {
        RoleJpaEntity r = new RoleJpaEntity();
        r.id = id;
        r.organisationId = organisationId;
        r.code = code;
        r.nom = nom;
        r.description = description;
        r.actif = true;
        r.createdAt = now;
        r.updatedAt = now;
        return r;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }
}

