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
public class MarquerRetourEnvoyeUseCase {

    private final RetourFournisseurJpaRepository retours;
    private final AuditWriter auditWriter;

    public MarquerRetourEnvoyeUseCase(RetourFournisseurJpaRepository retours, AuditWriter auditWriter) {
        this.retours = Objects.requireNonNull(retours);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public void execute(UUID organisationId, UUID retourId, UUID actorId) {
        RetourFournisseurJpaEntity r = retours.findByOrganisationIdAndId(organisationId, retourId)
                .orElseThrow(() -> new BusinessRuleViolationException("Retour introuvable"));
        if (!"VALIDE".equals(r.getStatut())) {
            throw new BusinessRuleViolationException("Retour non envoyable");
        }
        r.marquerEnvoye();
        Instant now = Instant.now();
        auditWriter.write(AuditEvent.simple(
                organisationId, now, null, null, null,
                null, null, "RETOUR_FOURNISSEUR_ENVOYE", "RetourFournisseur", retourId.toString(), null,
                Map.of()
        ));
    }
}

