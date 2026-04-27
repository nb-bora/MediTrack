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
public class CloturerRetourFournisseurUseCase {

    private final RetourFournisseurJpaRepository retours;
    private final AuditWriter auditWriter;

    public CloturerRetourFournisseurUseCase(RetourFournisseurJpaRepository retours, AuditWriter auditWriter) {
        this.retours = Objects.requireNonNull(retours);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public void execute(UUID organisationId, UUID retourId, UUID actorId) {
        RetourFournisseurJpaEntity r = retours.findByOrganisationIdAndId(organisationId, retourId)
                .orElseThrow(() -> new BusinessRuleViolationException("Retour introuvable"));
        if (!"ENVOYE".equals(r.getStatut())) {
            throw new BusinessRuleViolationException("Retour non clôturable");
        }
        r.cloturer();
        Instant now = Instant.now();
        auditWriter.write(AuditEvent.simple(
                organisationId, now, null, null, null,
                null, null, "RETOUR_FOURNISSEUR_CLOTURE", "RetourFournisseur", retourId.toString(), null,
                Map.of()
        ));
    }
}

