package cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "produit_substitut")
public class ProduitSubstitutJpaEntity {

    @EmbeddedId
    private ProduitSubstitutId id;

    @Column(name = "niveau", nullable = false, length = 20)
    private String niveau;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    protected ProduitSubstitutJpaEntity() {
    }

    public static ProduitSubstitutJpaEntity link(ProduitSubstitutId id, String niveau, UUID createdBy, Instant now) {
        ProduitSubstitutJpaEntity e = new ProduitSubstitutJpaEntity();
        e.id = id;
        e.niveau = niveau;
        e.createdBy = createdBy;
        e.createdAt = now;
        return e;
    }
}

