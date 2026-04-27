package cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "profil_taxe")
public class ProfilTaxeJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "taux_tva", nullable = false, precision = 7, scale = 4)
    private BigDecimal tauxTva;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProfilTaxeJpaEntity() {
    }

    public static ProfilTaxeJpaEntity create(UUID id, UUID organisationId, String nom, BigDecimal tauxTva, Instant now) {
        ProfilTaxeJpaEntity e = new ProfilTaxeJpaEntity();
        e.id = id;
        e.organisationId = organisationId;
        e.nom = nom;
        e.tauxTva = tauxTva;
        e.actif = true;
        e.createdAt = now;
        e.updatedAt = now;
        return e;
    }

    public UUID getId() {
        return id;
    }
}

