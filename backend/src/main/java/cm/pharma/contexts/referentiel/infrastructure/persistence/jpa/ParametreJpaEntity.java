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
}

