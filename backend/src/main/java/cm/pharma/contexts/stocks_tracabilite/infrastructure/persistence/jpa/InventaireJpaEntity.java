package cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventaire")
public class InventaireJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "statut", nullable = false, length = 20)
    private String statut;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "validated_at")
    private Instant validatedAt;

    @Column(name = "validated_by")
    private UUID validatedBy;

    protected InventaireJpaEntity() {
    }

    public static InventaireJpaEntity create(UUID id, UUID organisationId, UUID createdBy, Instant now) {
        InventaireJpaEntity i = new InventaireJpaEntity();
        i.id = id;
        i.organisationId = organisationId;
        i.statut = "OUVERT";
        i.createdAt = now;
        i.createdBy = createdBy;
        return i;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public String getStatut() {
        return statut;
    }

    public void valider(UUID validatedBy, Instant now) {
        this.statut = "VALIDE";
        this.validatedBy = validatedBy;
        this.validatedAt = now;
    }
}

