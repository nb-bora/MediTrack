package cm.pharma.shared.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Base pour toutes les entités du domaine.
 *
 * <p>Règles :
 * <ul>
 *   <li>Identité technique en {@link UUID}.</li>
 *   <li>Égalité fondée sur l’identité (et non sur tous les champs).</li>
 *   <li>Aucune dépendance vers Spring/JPA.</li>
 * </ul>
 * </p>
 */
public abstract class EntityBase {

    private final UUID id;

    protected EntityBase(UUID id) {
        this.id = Objects.requireNonNull(id, "id est requis");
    }

    public final UUID id() {
        return id;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;
        EntityBase that = (EntityBase) o;
        return id.equals(that.id);
    }

    @Override
    public final int hashCode() {
        return id.hashCode();
    }
}

