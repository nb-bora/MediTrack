package cm.pharma.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Représente un montant monétaire.
 *
 * <p>Décision : on stocke en {@link BigDecimal} avec une échelle configurable.
 * En V1 (XAF), les montants sont généralement entiers, mais l’échelle permet de
 * gérer des règles futures (TVA, prorata, unités fractionnables).</p>
 */
public final class Money implements ValueObject {

    public static final String DEFAULT_CURRENCY = "XAF";

    private final BigDecimal amount;
    private final String currency;

    private Money(BigDecimal amount, String currency) {
        this.amount = normalize(Objects.requireNonNull(amount, "amount est requis"));
        this.currency = Objects.requireNonNull(currency, "currency est requise");
        if (this.currency.isBlank()) {
            throw new IllegalArgumentException("currency ne peut pas être vide");
        }
        if (this.amount.signum() < 0) {
            throw new BusinessRuleViolationException("Un montant ne peut pas être négatif");
        }
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount, DEFAULT_CURRENCY);
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public BigDecimal amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.signum() < 0) {
            throw new BusinessRuleViolationException("Résultat négatif interdit (Money)");
        }
        return new Money(result, this.currency);
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "other est requis");
        if (!this.currency.equals(other.currency)) {
            throw new BusinessRuleViolationException("Devises incompatibles : " + this.currency + " vs " + other.currency);
        }
    }

    private static BigDecimal normalize(BigDecimal value) {
        return value.setScale(4, RoundingMode.HALF_UP);
    }
}

