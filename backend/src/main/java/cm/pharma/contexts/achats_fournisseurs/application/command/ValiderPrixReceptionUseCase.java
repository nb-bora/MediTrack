package cm.pharma.contexts.achats_fournisseurs.application.command;

import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.ReceptionFournisseurJpaEntity;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.ReceptionFournisseurJpaRepository;
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
public class ValiderPrixReceptionUseCase {

    private final ReceptionFournisseurJpaRepository receptions;
    private final AuditWriter auditWriter;

    public ValiderPrixReceptionUseCase(ReceptionFournisseurJpaRepository receptions, AuditWriter auditWriter) {
        this.receptions = Objects.requireNonNull(receptions);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public void execute(UUID organisationId, UUID receptionId, boolean valider, String message, UUID actorId) {
        ReceptionFournisseurJpaEntity r = receptions.findByOrganisationIdAndId(organisationId, receptionId)
                .orElseThrow(() -> new BusinessRuleViolationException("Réception introuvable"));
        if (!"EN_ATTENTE".equals(r.getStatutValidationPrix())) {
            throw new BusinessRuleViolationException("Réception non en attente de validation prix");
        }
        r.setStatutValidationPrix(valider ? "VALIDE" : "REFUSE");

        Instant now = Instant.now();
        auditWriter.write(AuditEvent.simple(
                organisationId, now, null, null, null,
                null, null, valider ? "RECEPTION_PRIX_VALIDE" : "RECEPTION_PRIX_REFUSE",
                "ReceptionFournisseur", receptionId.toString(), message,
                Map.of()
        ));
    }
}

