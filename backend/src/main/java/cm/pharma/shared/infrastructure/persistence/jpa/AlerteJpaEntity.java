package cm.pharma.shared.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alerte")
public class AlerteJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "type_alerte", nullable = false, length = 40)
    private String typeAlerte;

    @Column(name = "niveau", nullable = false, length = 20)
    private String niveau;

    @Column(name = "entite", length = 60)
    private String entite;

    @Column(name = "entite_id")
    private String entiteId;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @Column(name = "resolution_message")
    private String resolutionMessage;

    protected AlerteJpaEntity() {
    }

    public record AlerteInit(
            UUID id,
            UUID organisationId,
            String typeAlerte,
            String niveau,
            String entite,
            String entiteId,
            String message,
            UUID createdBy,
            Instant now
    ) {
    }

    public static AlerteJpaEntity open(AlerteInit init) {
        AlerteJpaEntity a = new AlerteJpaEntity();
        a.id = init.id();
        a.organisationId = init.organisationId();
        a.typeAlerte = init.typeAlerte();
        a.niveau = init.niveau();
        a.entite = init.entite();
        a.entiteId = init.entiteId();
        a.message = init.message();
        a.createdAt = init.now();
        a.createdBy = init.createdBy();
        return a;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public String getTypeAlerte() {
        return typeAlerte;
    }

    public String getEntite() {
        return entite;
    }

    public String getEntiteId() {
        return entiteId;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void resolve(UUID actorId, String message, Instant now) {
        this.resolvedAt = now;
        this.resolvedBy = actorId;
        this.resolutionMessage = message;
    }
}

