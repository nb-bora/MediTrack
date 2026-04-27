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
public class OuvrirSessionCaisseUseCase {

    private final SessionCaisseJpaRepository sessions;
    private final AuditWriter auditWriter;

    public OuvrirSessionCaisseUseCase(SessionCaisseJpaRepository sessions, AuditWriter auditWriter) {
        this.sessions = Objects.requireNonNull(sessions);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public UUID execute(UUID organisationId, String posteNom, UUID caissierId, BigDecimal fondInitial, String devise) {
        if (fondInitial == null || fondInitial.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("Fond initial invalide");
        }
        sessions.findOuverteParPoste(organisationId, posteNom).ifPresent(s -> {
            throw new BusinessRuleViolationException("Une session caisse est déjà ouverte sur ce poste");
        });
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        sessions.save(SessionCaisseJpaEntity.open(id, organisationId, posteNom, caissierId, fondInitial, devise, now));

        auditWriter.write(AuditEvent.simple(
                organisationId, now, caissierId, null, null,
                posteNom, null, "CAISSE_OUVERTE", "SessionCaisse", id.toString(), null,
                Map.of("fond_initial", fondInitial, "devise", devise)
        ));
        return id;
    }
}

