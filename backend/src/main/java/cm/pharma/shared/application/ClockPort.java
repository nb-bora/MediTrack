package cm.pharma.shared.application;

import java.time.Instant;

/**
 * Port temps pour rendre les cas d’usage testables.
 *
 * <p>On évite d’appeler directement {@link Instant#now()} dans l’application,
 * afin de maîtriser le temps en tests et garantir des comportements déterministes
 * (expiration de session, horodatage d’audit, etc.).</p>
 */
public interface ClockPort {
    Instant now();
}

