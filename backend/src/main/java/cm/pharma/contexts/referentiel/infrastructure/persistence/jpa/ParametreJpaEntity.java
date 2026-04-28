package cm.pharma.contexts.referentiel.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "parametre")
public class ParametreJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "cle", nullable = false, length = 120)
    private String cle;

    @Column(name = "valeur", nullable = false)
    private String valeur;

    @Column(name = "type_valeur", nullable = false, length = 20)
    private String typeValeur;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ParametreJpaEntity() {
    }

    public static ParametreJpaEntity create(UUID id, UUID organisationId, String cle, String valeur, String typeValeur, String description, Instant now) {
        ParametreJpaEntity e = new ParametreJpaEntity();
        e.id = id;
        e.organisationId = organisationId;
        e.cle = cle;
        e.valeur = valeur;
        e.typeValeur = typeValeur;
        e.description = description;
        e.createdAt = now;
        e.updatedAt = now;
        return e;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public String getCle() {
        return cle;
    }

    public String getValeur() {
        return valeur;
    }

    public String getTypeValeur() {
        return typeValeur;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(String valeur, String typeValeur, String description, Instant now) {
        this.valeur = valeur;
        this.typeValeur = typeValeur;
        this.description = description;
        this.updatedAt = now;
    }
}

