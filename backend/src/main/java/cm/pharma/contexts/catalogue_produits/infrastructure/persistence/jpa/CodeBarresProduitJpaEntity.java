package cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "code_barres_produit")
public class CodeBarresProduitJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "produit_id", nullable = false)
    private UUID produitId;

    @Column(name = "ean13", nullable = false, length = 13)
    private String ean13;

    @Column(name = "libelle")
    private String libelle;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CodeBarresProduitJpaEntity() {
    }

    public static CodeBarresProduitJpaEntity create(UUID id, UUID produitId, String ean13, String libelle, Instant now) {
        CodeBarresProduitJpaEntity e = new CodeBarresProduitJpaEntity();
        e.id = id;
        e.produitId = produitId;
        e.ean13 = ean13;
        e.libelle = libelle;
        e.actif = true;
        e.createdAt = now;
        e.updatedAt = now;
        return e;
    }

    public UUID getProduitId() {
        return produitId;
    }
}

