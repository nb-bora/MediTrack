package cm.pharma.contexts.referentiel.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sequence_numerotation")
public class SequenceNumerotationJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "type_document", nullable = false, length = 30)
    private String typeDocument;

    @Column(name = "format", nullable = false)
    private String format;

    @Column(name = "compteur_courant", nullable = false)
    private int compteurCourant;

    @Column(name = "reset_frequence", nullable = false, length = 10)
    private String resetFrequence;

    @Column(name = "reset_dernier")
    private Instant resetDernier;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SequenceNumerotationJpaEntity() {
    }

    public static SequenceNumerotationJpaEntity create(UUID id, UUID organisationId, String typeDocument, String format, Instant now) {
        SequenceNumerotationJpaEntity e = new SequenceNumerotationJpaEntity();
        e.id = id;
        e.organisationId = organisationId;
        e.typeDocument = typeDocument;
        e.format = format;
        e.compteurCourant = 0;
        e.resetFrequence = "ANNUEL";
        e.resetDernier = null;
        e.createdAt = now;
        e.updatedAt = now;
        return e;
    }

    public String getFormat() {
        return format;
    }

    public int nextAndIncrement(Instant now) {
        this.compteurCourant = this.compteurCourant + 1;
        this.updatedAt = now;
        return this.compteurCourant;
    }
}

