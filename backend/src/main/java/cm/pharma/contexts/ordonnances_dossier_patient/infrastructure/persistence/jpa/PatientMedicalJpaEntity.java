package cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "patient_medical")
public class PatientMedicalJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "allergies")
    private String allergies;

    @Column(name = "pathologies_chroniques")
    private String pathologiesChroniques;

    @Column(name = "medecin_traitant")
    private String medecinTraitant;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    protected PatientMedicalJpaEntity() {
    }

    public static PatientMedicalJpaEntity create(UUID id, UUID organisationId, UUID patientId, Instant now) {
        PatientMedicalJpaEntity pm = new PatientMedicalJpaEntity();
        pm.id = id;
        pm.organisationId = organisationId;
        pm.patientId = patientId;
        pm.updatedAt = now;
        return pm;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public String getAllergies() {
        return allergies;
    }

    public String getPathologiesChroniques() {
        return pathologiesChroniques;
    }

    public String getMedecinTraitant() {
        return medecinTraitant;
    }

    public void update(String allergies, String pathologiesChroniques, String medecinTraitant, UUID actorId, Instant now) {
        this.allergies = allergies;
        this.pathologiesChroniques = pathologiesChroniques;
        this.medecinTraitant = medecinTraitant;
        this.updatedBy = actorId;
        this.updatedAt = now;
    }
}

