package cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "categorie_produit")
public class CategorieProduitJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CategorieProduitJpaEntity() {
    }

    public static CategorieProduitJpaEntity create(UUID id, UUID organisationId, UUID parentId, String nom, Instant now) {
        CategorieProduitJpaEntity c = new CategorieProduitJpaEntity();
        c.id = id;
        c.organisationId = organisationId;
        c.parentId = parentId;
        c.nom = nom;
        c.actif = true;
        c.createdAt = now;
        c.updatedAt = now;
        return c;
    }

    public UUID getId() {
        return id;
    }

    public UUID getParentId() {
        return parentId;
    }

    public String getNom() {
        return nom;
    }
}

