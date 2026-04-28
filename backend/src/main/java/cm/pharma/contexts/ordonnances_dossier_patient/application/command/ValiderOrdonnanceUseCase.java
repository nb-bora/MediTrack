package cm.pharma.contexts.ordonnances_dossier_patient.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceJpaEntity;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceJpaRepository;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnancePieceJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValiderOrdonnanceUseCase {

    private final OrdonnanceJpaRepository ordonnances;
    private final OrdonnancePieceJpaRepository pieces;
    private final AuditWriter auditWriter;

    public ValiderOrdonnanceUseCase(OrdonnanceJpaRepository ordonnances, OrdonnancePieceJpaRepository pieces, AuditWriter auditWriter) {
        this.ordonnances = Objects.requireNonNull(ordonnances);
        this.pieces = Objects.requireNonNull(pieces);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public void valider(UUID organisationId, UUID ordonnanceId, UUID actorId, String posteNom) {
        OrdonnanceJpaEntity o = ordonnances.findByOrganisationIdAndId(organisationId, ordonnanceId)
                .orElseThrow(() -> new BusinessRuleViolationException("Ordonnance introuvable"));
        if (!"EN_ATTENTE_VALIDATION".equals(o.getStatut())) {
            throw new BusinessRuleViolationException("Ordonnance non validable");
        }
        Instant now = Instant.now();
        if (o.getDateExpiration().isBefore(LocalDate.now())) {
            throw new BusinessRuleViolationException("Ordonnance expirée");
        }
        if (pieces.findByOrganisationIdAndOrdonnanceId(organisationId, ordonnanceId).isEmpty()) {
            throw new BusinessRuleViolationException("Pièce ordonnance requise (scan) avant validation");
        }
        o.valider(actorId, now);
        auditWriter.write(AuditEvent.simple(
                organisationId, now, actorId, null, null,
                posteNom, null, "ORDONNANCE_VALIDEE", "Ordonnance", ordonnanceId.toString(), null,
                Map.of()
        ));
    }

    @Transactional
    public void refuser(UUID organisationId, UUID ordonnanceId, String motif, UUID actorId, String posteNom) {
        if (motif == null || motif.isBlank()) {
            throw new BusinessRuleViolationException("Motif de refus requis");
        }
        OrdonnanceJpaEntity o = ordonnances.findByOrganisationIdAndId(organisationId, ordonnanceId)
                .orElseThrow(() -> new BusinessRuleViolationException("Ordonnance introuvable"));
        if (!"EN_ATTENTE_VALIDATION".equals(o.getStatut())) {
            throw new BusinessRuleViolationException("Ordonnance non refus-able");
        }
        Instant now = Instant.now();
        o.refuser(motif, actorId, now);
        auditWriter.write(AuditEvent.simple(
                organisationId, now, actorId, null, null,
                posteNom, null, "ORDONNANCE_REFUSEE", "Ordonnance", ordonnanceId.toString(), motif,
                Map.of()
        ));
    }
}

