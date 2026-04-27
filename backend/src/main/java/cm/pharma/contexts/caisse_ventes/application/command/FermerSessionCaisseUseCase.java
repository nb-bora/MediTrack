package cm.pharma.contexts.caisse_ventes.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.SessionCaisseJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.SessionCaisseJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FermerSessionCaisseUseCase {

    private final SessionCaisseJpaRepository sessions;
    private final AuditWriter auditWriter;

    public FermerSessionCaisseUseCase(SessionCaisseJpaRepository sessions, AuditWriter auditWriter) {
        this.sessions = Objects.requireNonNull(sessions);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public void execute(UUID organisationId, UUID sessionId, String posteNom, UUID actorId, BigDecimal montantReel, String motifEcart) {
        if (montantReel == null || montantReel.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("Montant réel invalide");
        }
        SessionCaisseJpaEntity s = sessions.findById(sessionId)
                .orElseThrow(() -> new BusinessRuleViolationException("Session caisse introuvable"));
        if (!s.getOrganisationId().equals(organisationId)) {
            throw new BusinessRuleViolationException("Session caisse hors organisation");
        }
        if (!"OUVERTE".equals(s.getStatut())) {
            throw new BusinessRuleViolationException("Session caisse déjà fermée");
        }
        if (!s.getPosteNom().equals(posteNom)) {
            throw new BusinessRuleViolationException("Session caisse d’un autre poste");
        }

        Instant now = Instant.now();
        s.close(montantReel, motifEcart, now);

        auditWriter.write(AuditEvent.simple(
                organisationId, now, actorId, null, null,
                posteNom, null, "CAISSE_FERMEE", "SessionCaisse", sessionId.toString(), motifEcart,
                Map.of("montant_reel_fermeture", montantReel)
        ));
    }
}

