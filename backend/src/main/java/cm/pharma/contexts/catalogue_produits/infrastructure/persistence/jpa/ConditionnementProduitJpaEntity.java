package cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conditionnement_produit")
public class ConditionnementProduitJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "produit_id", nullable = false)
    private UUID produitId;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "unite_base_nom", nullable = false)
    private String uniteBaseNom;

    @Column(name = "quantite_unite_base", nullable = false)
    private int quantiteUniteBase;

    @Column(name = "est_principal", nullable = false)
    private boolean estPrincipal;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ConditionnementProduitJpaEntity() {
    }

    public static ConditionnementProduitJpaEntity create(UUID id, UUID produitId, String nom, String uniteBaseNom,
                                                         int quantiteUniteBase, boolean estPrincipal, Instant now) {
        ConditionnementProduitJpaEntity c = new ConditionnementProduitJpaEntity();
        c.id = id;
        c.produitId = produitId;
        c.nom = nom;
        c.uniteBaseNom = uniteBaseNom;
        c.quantiteUniteBase = quantiteUniteBase;
        c.estPrincipal = estPrincipal;
        c.actif = true;
        c.createdAt = now;
        c.updatedAt = now;
        return c;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProduitId() {
        return produitId;
    }

    public boolean isEstPrincipal() {
        return estPrincipal;
    }

    public void setEstPrincipal(boolean estPrincipal, Instant now) {
        this.estPrincipal = estPrincipal;
        this.updatedAt = now;
    }
}

