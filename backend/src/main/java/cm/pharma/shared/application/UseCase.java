package cm.pharma.shared.application;

/**
 * Contrat minimal d’un cas d’usage (Use Case).
 *
 * <p>Un use case orchestre des règles métier (domaine) et des accès externes via des ports
 * (repositories, services d’impression, stockage fichiers, etc.).</p>
 */
@FunctionalInterface
public interface UseCase<I, O> {
    O execute(I input);
}

