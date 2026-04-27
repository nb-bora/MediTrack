package cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class ProduitSubstitutId implements Serializable {

    @Column(name = "produit_id", nullable = false)
    private UUID produitId;

    @Column(name = "substitut_produit_id", nullable = false)
    private UUID substitutProduitId;

    protected ProduitSubstitutId() {
    }

    public ProduitSubstitutId(UUID produitId, UUID substitutProduitId) {
        this.produitId = produitId;
        this.substitutProduitId = substitutProduitId;
    }

    public UUID getProduitId() {
        return produitId;
    }

    public UUID getSubstitutProduitId() {
        return substitutProduitId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProduitSubstitutId that = (ProduitSubstitutId) o;
        return java.util.Objects.equals(produitId, that.produitId)
                && java.util.Objects.equals(substitutProduitId, that.substitutProduitId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(produitId, substitutProduitId);
    }
}

