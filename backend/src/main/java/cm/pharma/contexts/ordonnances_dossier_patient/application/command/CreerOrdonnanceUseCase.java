package cm.pharma.contexts.ordonnances_dossier_patient.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceJpaEntity;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceJpaRepository;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceLigneJpaEntity;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceLigneJpaRepository;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.PatientJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreerOrdonnanceUseCase {

    private final PatientJpaRepository patients;
    private final OrdonnanceJpaRepository ordonnances;
    private final OrdonnanceLigneJpaRepository lignes;
    private final AuditWriter auditWriter;

    public CreerOrdonnanceUseCase(PatientJpaRepository patients, OrdonnanceJpaRepository ordonnances, OrdonnanceLigneJpaRepository lignes, AuditWriter auditWriter) {
        this.patients = Objects.requireNonNull(patients);
        this.ordonnances = Objects.requireNonNull(ordonnances);
        this.lignes = Objects.requireNonNull(lignes);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public UUID execute(CreerOrdonnanceCommand cmd) {
        Objects.requireNonNull(cmd);
        patients.findByOrganisationIdAndId(cmd.organisationId(), cmd.patientId())
                .orElseThrow(() -> new BusinessRuleViolationException("Patient introuvable"));
        if (cmd.datePrescription() == null) {
            throw new BusinessRuleViolationException("Date prescription requise");
        }
        LocalDate expiration = cmd.dateExpiration() == null ? cmd.datePrescription().plusDays(30) : cmd.dateExpiration();
        if (expiration.isBefore(cmd.datePrescription())) {
            throw new BusinessRuleViolationException("Expiration invalide");
        }
        if (cmd.lignes() == null || cmd.lignes().isEmpty()) {
            throw new BusinessRuleViolationException("Ordonnance sans lignes");
        }

        Instant now = Instant.now();
        UUID ordonnanceId = UUID.randomUUID();
        ordonnances.save(OrdonnanceJpaEntity.create(new OrdonnanceJpaEntity.OrdonnanceInit(
                ordonnanceId,
                cmd.organisationId(),
                cmd.patientId(),
                cmd.prescripteurId(),
                cmd.datePrescription(),
                expiration,
                cmd.ordonnanceParentId(),
                cmd.creePar(),
                now
        )));

        for (Ligne l : cmd.lignes()) {
            if (l.quantitePrescrite() <= 0) {
                throw new BusinessRuleViolationException("Quantité prescrite invalide");
            }
            lignes.save(OrdonnanceLigneJpaEntity.create(new OrdonnanceLigneJpaEntity.OrdonnanceLigneInit(
                    UUID.randomUUID(),
                    cmd.organisationId(),
                    ordonnanceId,
                    l.produitId(),
                    l.quantitePrescrite(),
                    l.posologie(),
                    l.dureeJours()
            )));
        }

        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, cmd.creePar(), null, null,
                cmd.posteNom(), null, "ORDONNANCE_CREEE", "Ordonnance", ordonnanceId.toString(), null,
                Map.of("patient_id", cmd.patientId(), "nb_lignes", cmd.lignes().size())
        ));
        return ordonnanceId;
    }

    public record CreerOrdonnanceCommand(
            UUID organisationId,
            UUID patientId,
            UUID prescripteurId,
            LocalDate datePrescription,
            LocalDate dateExpiration,
            UUID ordonnanceParentId,
            List<Ligne> lignes,
            UUID creePar,
            String posteNom
    ) {
    }

    public record Ligne(UUID produitId, int quantitePrescrite, String posologie, Integer dureeJours) {
    }
}

