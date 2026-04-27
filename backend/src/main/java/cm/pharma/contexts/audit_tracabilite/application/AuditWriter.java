package cm.pharma.contexts.audit_tracabilite.application;

import cm.pharma.contexts.audit_tracabilite.infrastructure.persistence.jpa.EvenementAuditJpaEntity;
import cm.pharma.contexts.audit_tracabilite.infrastructure.persistence.jpa.EvenementAuditJpaRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Écriture d’audit “fonctionnel” (append-only) dans {@code evenement_audit}.
 *
 * <p>Le schéma impose l’immutabilité en base (triggers Flyway V2), donc ce writer se limite
 * à insérer. Tous les modules doivent appeler ce composant pour tracer les actions sensibles.</p>
 */
@Component
public class AuditWriter {

    private final EvenementAuditJpaRepository audits;

    public AuditWriter(EvenementAuditJpaRepository audits) {
        this.audits = audits;
    }

    public void write(AuditEvent event) {
        audits.save(EvenementAuditJpaEntity.from(
                UUID.randomUUID(),
                event.organisationId(),
                event.horodatage(),
                event.utilisateurId(),
                event.utilisateurNom(),
                event.utilisateurRole(),
                event.poste(),
                event.adresseIp(),
                event.action(),
                event.entite(),
                event.entiteId(),
                event.motif(),
                event.detailsJson()
        ));
    }

    public record AuditEvent(
            UUID organisationId,
            Instant horodatage,
            UUID utilisateurId,
            String utilisateurNom,
            String utilisateurRole,
            String poste,
            String adresseIp,
            String action,
            String entite,
            String entiteId,
            String motif,
            String detailsJson
    ) {
        public static AuditEvent simple(UUID organisationId, Instant at, UUID userId, String userName, String role,
                                        String poste, String ip, String action, String entite, String entiteId,
                                        String motif, Map<String, Object> details) {
            return new AuditEvent(
                    organisationId, at, userId, userName, role, poste, ip, action, entite, entiteId, motif,
                    JsonUtils.toJson(details)
            );
        }
    }
}

