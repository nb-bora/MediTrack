package cm.pharma.shared.application;

import cm.pharma.shared.infrastructure.persistence.jpa.AlerteJpaEntity;
import cm.pharma.shared.infrastructure.persistence.jpa.AlerteJpaRepository;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlerteService {

    private final AlerteJpaRepository alertes;

    public AlerteService(AlerteJpaRepository alertes) {
        this.alertes = Objects.requireNonNull(alertes);
    }

    @Transactional
    public UUID openDedup(UUID organisationId, String type, String niveau, String entite, String entiteId, String message, UUID actorId) {
        return alertes.findByOrganisationIdAndTypeAlerteAndEntiteAndEntiteIdAndResolvedAtIsNull(organisationId, type, entite, entiteId)
                .map(AlerteJpaEntity::getId)
                .orElseGet(() -> {
                    Instant now = Instant.now();
                    UUID id = UUID.randomUUID();
                    alertes.save(AlerteJpaEntity.open(new AlerteJpaEntity.AlerteInit(
                            id, organisationId, type, niveau, entite, entiteId, message, actorId, now
                    )));
                    return id;
                });
    }

    @Transactional
    public void resolve(UUID organisationId, UUID alerteId, UUID actorId, String resolutionMessage) {
        AlerteJpaEntity a = alertes.findById(alerteId).orElseThrow();
        if (!a.getOrganisationId().equals(organisationId)) {
            throw new IllegalArgumentException("Alerte hors organisation");
        }
        a.resolve(actorId, resolutionMessage, Instant.now());
    }

    /**
     * Résout une alerte ouverte identifiée par sa clé de déduplication.
     *
     * <p>Utile lorsque l’on ne persiste pas l’ID de l’alerte côté workflow.</p>
     */
    @Transactional
    public void resolveDedup(UUID organisationId, String type, String entite, String entiteId, UUID actorId, String resolutionMessage) {
        alertes.findByOrganisationIdAndTypeAlerteAndEntiteAndEntiteIdAndResolvedAtIsNull(organisationId, type, entite, entiteId)
                .ifPresent(a -> a.resolve(actorId, resolutionMessage, Instant.now()));
    }
}

