package cm.pharma.shared.domain;

/**
 * Exception levée lorsqu’une règle métier est violée.
 *
 * <p>Principe : le domaine exprime des invariants (ex: « stock jamais négatif »,
 * « un lot en quarantaine est invendable »). Quand un invariant est enfreint,
 * cette exception est levée et traduite dans les couches supérieures (application/interfaces)
 * en un message utilisateur ou une réponse API adaptée.</p>
 */
public final class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}

