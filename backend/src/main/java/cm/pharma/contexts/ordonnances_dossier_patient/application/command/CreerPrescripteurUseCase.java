package cm.pharma.contexts.ordonnances_dossier_patient.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.PrescripteurJpaEntity;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.PrescripteurJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreerPrescripteurUseCase {

    private final PrescripteurJpaRepository prescripteurs;
    private final AuditWriter auditWriter;

    public CreerPrescripteurUseCase(PrescripteurJpaRepository prescripteurs, AuditWriter auditWriter) {
        this.prescripteurs = Objects.requireNonNull(prescripteurs);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public UUID execute(CreerPrescripteurCommand cmd) {
        Objects.requireNonNull(cmd);
        if (cmd.nom() == null || cmd.nom().isBlank()) {
            throw new BusinessRuleViolationException("Nom prescripteur requis");
        }
        // Dédup simple (nom + structure)
        String structure = cmd.structure() == null ? "" : cmd.structure().trim();
        if (prescripteurs.existsByOrganisationIdAndNomIgnoreCaseAndStructureIgnoreCase(cmd.organisationId(), cmd.nom().trim(), structure)) {
            throw new BusinessRuleViolationException("Prescripteur déjà existant (nom + structure)");
        }

        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        prescripteurs.save(PrescripteurJpaEntity.create(new PrescripteurJpaEntity.PrescripteurInit(
                id,
                cmd.organisationId(),
                cmd.nom().trim(),
                structure.isBlank() ? null : structure,
                cmd.telephone(),
                now
        )));

        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, cmd.creePar(), null, null,
                cmd.posteNom(), null, "PRESCRIPTEUR_CREE", "Prescripteur", id.toString(), null,
                Map.of("nom", cmd.nom(), "structure", cmd.structure())
        ));
        return id;
    }

    public record CreerPrescripteurCommand(UUID organisationId, String nom, String structure, String telephone, UUID creePar, String posteNom) {
    }
}

