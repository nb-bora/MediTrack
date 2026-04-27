package cm.pharma.shared.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine.
 *
 * <p>Un événement de domaine représente un fait métier déjà arrivé, utile pour
 * déclencher des réactions (ex: « Lot périmé automatiquement », « Vente finalisée »)
 * sans coupler les bounded contexts par des dépendances techniques.</p>
 */
public interface DomainEvent {

    UUID eventId();

    Instant occurredAt();
}

