package cm.pharma.contexts.achats_fournisseurs.application.command;

import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.RetourFournisseurJpaEntity;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.RetourFournisseurJpaRepository;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreerRetourFournisseurUseCase {

    private final RetourFournisseurJpaRepository retours;
    private final AuditWriter auditWriter;

    public CreerRetourFournisseurUseCase(RetourFournisseurJpaRepository retours, AuditWriter auditWriter) {
        this.retours = Objects.requireNonNull(retours);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public UUID execute(UUID organisationId, UUID fournisseurId, String motif, String referenceDocument, UUID actorId) {
        if (motif == null || motif.isBlank()) {
            throw new BusinessRuleViolationException("Motif requis");
        }
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        retours.save(RetourFournisseurJpaEntity.create(id, organisationId, fournisseurId, motif.trim(), referenceDocument, actorId, now));

        auditWriter.write(AuditEvent.simple(
                organisationId, now, null, null, null,
                null, null, "RETOUR_FOURNISSEUR_CREE", "RetourFournisseur", id.toString(), motif,
                Map.of("fournisseur_id", fournisseurId)
        ));
        return id;
    }
}

