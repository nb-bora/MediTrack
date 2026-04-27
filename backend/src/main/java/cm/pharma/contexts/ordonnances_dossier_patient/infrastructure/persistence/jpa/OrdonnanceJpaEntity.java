package cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "ordonnance")
public class OrdonnanceJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "prescripteur_id")
    private UUID prescripteurId;

    @Column(name = "date_prescription", nullable = false)
    private LocalDate datePrescription;

    @Column(name = "date_expiration", nullable = false)
    private LocalDate dateExpiration;

    @Column(name = "statut", nullable = false, length = 30)
    private String statut;

    @Column(name = "ordonnance_parent_id")
    private UUID ordonnanceParentId;

    @Column(name = "motif_refus")
    private String motifRefus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "validated_at")
    private Instant validatedAt;

    @Column(name = "validated_by")
    private UUID validatedBy;

    protected OrdonnanceJpaEntity() {
    }

    public record OrdonnanceInit(
            UUID id,
            UUID organisationId,
            UUID patientId,
            UUID prescripteurId,
            LocalDate datePrescription,
            LocalDate dateExpiration,
            UUID ordonnanceParentId,
            UUID createdBy,
            Instant now
    ) {
    }

    public static OrdonnanceJpaEntity create(OrdonnanceInit init) {
        OrdonnanceJpaEntity o = new OrdonnanceJpaEntity();
        o.id = init.id();
        o.organisationId = init.organisationId();
        o.patientId = init.patientId();
        o.prescripteurId = init.prescripteurId();
        o.datePrescription = init.datePrescription();
        o.dateExpiration = init.dateExpiration();
        o.ordonnanceParentId = init.ordonnanceParentId();
        o.statut = "EN_ATTENTE_VALIDATION";
        o.createdBy = init.createdBy();
        o.createdAt = init.now();
        return o;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public String getStatut() {
        return statut;
    }

    public void valider(UUID actorId, Instant now) {
        this.statut = "VALIDEE";
        this.validatedBy = actorId;
        this.validatedAt = now;
        this.motifRefus = null;
    }

    public void refuser(String motif, UUID actorId, Instant now) {
        this.statut = "REFUSEE";
        this.motifRefus = motif;
        this.validatedBy = actorId;
        this.validatedAt = now;
    }

    public void majStatutDispensation(int totalPrescrit, int totalDispense) {
        if (totalDispense <= 0) {
            return;
        }
        if (totalDispense >= totalPrescrit) {
            this.statut = "DISPENSEE";
        } else {
            this.statut = "PARTIELLEMENT_DISPENSEE";
        }
    }
}

