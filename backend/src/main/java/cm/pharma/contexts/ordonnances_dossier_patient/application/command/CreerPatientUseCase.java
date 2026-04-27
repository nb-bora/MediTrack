package cm.pharma.contexts.ordonnances_dossier_patient.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.PatientJpaEntity;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.PatientJpaRepository;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.PatientMedicalJpaEntity;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.PatientMedicalJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreerPatientUseCase {

    private final PatientJpaRepository patients;
    private final PatientMedicalJpaRepository medical;
    private final AuditWriter auditWriter;

    public CreerPatientUseCase(PatientJpaRepository patients, PatientMedicalJpaRepository medical, AuditWriter auditWriter) {
        this.patients = Objects.requireNonNull(patients);
        this.medical = Objects.requireNonNull(medical);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public UUID execute(CreerPatientCommand cmd) {
        Objects.requireNonNull(cmd);
        if (cmd.nom() == null || cmd.nom().isBlank() || cmd.prenom() == null || cmd.prenom().isBlank()) {
            throw new BusinessRuleViolationException("Nom et prénom requis");
        }
        if (cmd.telephone() != null && !cmd.telephone().isBlank()) {
            patients.findByOrganisationIdAndTelephone(cmd.organisationId(), cmd.telephone().trim()).ifPresent(p -> {
                throw new BusinessRuleViolationException("Patient déjà existant (téléphone)");
            });
        }

        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        patients.save(PatientJpaEntity.create(new PatientJpaEntity.PatientInit(
                id,
                cmd.organisationId(),
                cmd.nom().trim(),
                cmd.prenom().trim(),
                cmd.dateNaissance(),
                cmd.sexe(),
                cmd.telephone() == null ? null : cmd.telephone().trim(),
                cmd.adresse(),
                cmd.assuranceOrganismeNom(),
                cmd.assuranceNumeroAdherent(),
                cmd.assuranceTauxCouverture(),
                now
        )));
        medical.save(PatientMedicalJpaEntity.create(UUID.randomUUID(), cmd.organisationId(), id, now));

        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, cmd.creePar(), null, null,
                cmd.posteNom(), null, "PATIENT_CREE", "Patient", id.toString(), null,
                Map.of("nom", cmd.nom(), "prenom", cmd.prenom(), "telephone", cmd.telephone())
        ));
        return id;
    }

    public record CreerPatientCommand(
            UUID organisationId,
            String nom,
            String prenom,
            java.time.LocalDate dateNaissance,
            String sexe,
            String telephone,
            String adresse,
            String assuranceOrganismeNom,
            String assuranceNumeroAdherent,
            Double assuranceTauxCouverture,
            UUID creePar,
            String posteNom
    ) {
    }
}

