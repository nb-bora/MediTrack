package cm.pharma.contexts.ordonnances_dossier_patient.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceJpaEntity;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceJpaRepository;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceLigneJpaEntity;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceLigneJpaRepository;
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
public class RenouvelerOrdonnanceUseCase {

    private final OrdonnanceJpaRepository ordonnances;
    private final OrdonnanceLigneJpaRepository lignes;
    private final AuditWriter auditWriter;

    public RenouvelerOrdonnanceUseCase(OrdonnanceJpaRepository ordonnances, OrdonnanceLigneJpaRepository lignes, AuditWriter auditWriter) {
        this.ordonnances = Objects.requireNonNull(ordonnances);
        this.lignes = Objects.requireNonNull(lignes);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public UUID execute(UUID organisationId, UUID ordonnanceSourceId, LocalDate datePrescription, LocalDate dateExpiration, UUID actorId, String posteNom) {
        OrdonnanceJpaEntity src = ordonnances.findByOrganisationIdAndId(organisationId, ordonnanceSourceId)
                .orElseThrow(() -> new BusinessRuleViolationException("Ordonnance source introuvable"));

        if (datePrescription == null) {
            throw new BusinessRuleViolationException("Date prescription requise");
        }
        LocalDate expiration = dateExpiration == null ? datePrescription.plusDays(30) : dateExpiration;
        if (expiration.isBefore(datePrescription)) {
            throw new BusinessRuleViolationException("Expiration invalide");
        }

        List<OrdonnanceLigneJpaEntity> srcLines = lignes.findByOrganisationIdAndOrdonnanceId(organisationId, ordonnanceSourceId);
        if (srcLines.isEmpty()) {
            throw new BusinessRuleViolationException("Ordonnance source sans lignes");
        }

        Instant now = Instant.now();
        UUID newId = UUID.randomUUID();
        ordonnances.save(OrdonnanceJpaEntity.create(new OrdonnanceJpaEntity.OrdonnanceInit(
                newId,
                organisationId,
                src.getPatientId(),
                src.getPrescripteurId(),
                datePrescription,
                expiration,
                ordonnanceSourceId,
                actorId,
                now
        )));

        for (OrdonnanceLigneJpaEntity l : srcLines) {
            lignes.save(OrdonnanceLigneJpaEntity.create(new OrdonnanceLigneJpaEntity.OrdonnanceLigneInit(
                    UUID.randomUUID(),
                    organisationId,
                    newId,
                    l.getProduitId(),
                    l.getQuantitePrescrite(),
                    l.getPosologie(),
                    l.getDureeJours()
            )));
        }

        auditWriter.write(AuditEvent.simple(
                organisationId, now, actorId, null, null,
                posteNom, null, "ORDONNANCE_RENOUVELEE", "Ordonnance", newId.toString(), null,
                Map.of("ordonnance_source_id", ordonnanceSourceId)
        ));
        return newId;
    }
}

