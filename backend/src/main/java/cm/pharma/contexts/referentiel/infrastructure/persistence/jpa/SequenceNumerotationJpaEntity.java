package cm.pharma.contexts.referentiel.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
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

    public UUID getId() {
        return id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public String getTypeDocument() {
        return typeDocument;
    }

    public String getFormat() {
        return format;
    }

    public int getCompteurCourant() {
        return compteurCourant;
    }

    public String getResetFrequence() {
        return resetFrequence;
    }

    public Instant getResetDernier() {
        return resetDernier;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public int nextAndIncrement(Instant now) {
        maybeReset(now);
        this.compteurCourant = this.compteurCourant + 1;
        this.updatedAt = now;
        return this.compteurCourant;
    }

    private void maybeReset(Instant now) {
        if (resetFrequence == null || resetFrequence.isBlank()) {
            return;
        }
        if (resetDernier == null) {
            resetDernier = now;
            return;
        }

        LocalDate last = LocalDate.ofInstant(resetDernier, ZoneId.systemDefault());
        LocalDate current = LocalDate.ofInstant(now, ZoneId.systemDefault());
        String freq = resetFrequence.toUpperCase();

        boolean mustReset = switch (freq) {
            case "MENSUEL" -> last.getYear() != current.getYear() || last.getMonthValue() != current.getMonthValue();
            case "ANNUEL" -> last.getYear() != current.getYear();
            default -> false;
        };

        if (mustReset) {
            compteurCourant = 0;
            resetDernier = now;
        }
    }

    public void updateConfig(String format, String resetFrequence, Instant now) {
        this.format = Objects.requireNonNull(format);
        this.resetFrequence = resetFrequence;
        this.updatedAt = now;
    }

    public void resetCounter(Instant now) {
        this.compteurCourant = 0;
        this.resetDernier = now;
        this.updatedAt = now;
    }
}

