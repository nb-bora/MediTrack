package cm.pharma.contexts.ordonnances_dossier_patient.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
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
public class MettreAJourPatientMedicalUseCase {

    private final PatientJpaRepository patients;
    private final PatientMedicalJpaRepository medical;
    private final AuditWriter auditWriter;

    public MettreAJourPatientMedicalUseCase(PatientJpaRepository patients, PatientMedicalJpaRepository medical, AuditWriter auditWriter) {
        this.patients = Objects.requireNonNull(patients);
        this.medical = Objects.requireNonNull(medical);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public void execute(MettreAJourPatientMedicalCommand cmd) {
        Objects.requireNonNull(cmd);
        patients.findByOrganisationIdAndId(cmd.organisationId(), cmd.patientId())
                .orElseThrow(() -> new BusinessRuleViolationException("Patient introuvable"));
        PatientMedicalJpaEntity pm = medical.findByPatientId(cmd.patientId())
                .orElseThrow(() -> new BusinessRuleViolationException("Fiche médicale introuvable"));

        Instant now = Instant.now();
        pm.update(cmd.allergies(), cmd.pathologiesChroniques(), cmd.medecinTraitant(), cmd.actorId(), now);

        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, cmd.actorId(), null, null,
                cmd.posteNom(), null, "PATIENT_MEDICAL_MAJ", "Patient", cmd.patientId().toString(), null,
                Map.of("allergies", cmd.allergies())
        ));
    }

    public record MettreAJourPatientMedicalCommand(
            UUID organisationId,
            UUID patientId,
            String allergies,
            String pathologiesChroniques,
            String medecinTraitant,
            UUID actorId,
            String posteNom
    ) {
    }
}

