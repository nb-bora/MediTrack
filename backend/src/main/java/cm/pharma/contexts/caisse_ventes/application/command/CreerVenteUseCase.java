package cm.pharma.contexts.caisse_ventes.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.SessionCaisseJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.SessionCaisseJpaRepository;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteJpaRepository;
import cm.pharma.contexts.referentiel.application.service.NumerotationService;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreerVenteUseCase {

    private final SessionCaisseJpaRepository sessions;
    private final VenteJpaRepository ventes;
    private final NumerotationService numerotation;
    private final AuditWriter auditWriter;

    public CreerVenteUseCase(
            SessionCaisseJpaRepository sessions,
            VenteJpaRepository ventes,
            NumerotationService numerotation,
            AuditWriter auditWriter
    ) {
        this.sessions = Objects.requireNonNull(sessions);
        this.ventes = Objects.requireNonNull(ventes);
        this.numerotation = Objects.requireNonNull(numerotation);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public UUID execute(UUID organisationId, String posteNom, UUID actorId, String devise) {
        SessionCaisseJpaEntity session = sessions.findOuverteParPoste(organisationId, posteNom)
                .orElseThrow(() -> new BusinessRuleViolationException("Aucune session caisse ouverte sur ce poste"));

        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        String numeroVente = numerotation.nextNumero(organisationId, "VENTE");
        String numeroTicket = numerotation.nextNumero(organisationId, "TICKET");
        ventes.save(VenteJpaEntity.create(new VenteJpaEntity.VenteInit(
                id,
                organisationId,
                session.getId(),
                numeroVente,
                numeroTicket,
                actorId,
                devise,
                now
        )));

        auditWriter.write(AuditEvent.simple(
                organisationId, now, actorId, null, null,
                posteNom, null, "VENTE_CREEE", "Vente", numeroVente, null,
                Map.of("ticket", numeroTicket)
        ));
        return id;
    }
}

