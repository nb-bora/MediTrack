package cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dispensation")
public class DispensationJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "ordonnance_id", nullable = false)
    private UUID ordonnanceId;

    @Column(name = "ordonnance_ligne_id", nullable = false)
    private UUID ordonnanceLigneId;

    @Column(name = "produit_id", nullable = false)
    private UUID produitId;

    @Column(name = "quantite", nullable = false)
    private int quantite;

    @Column(name = "lot_id")
    private UUID lotId;

    @Column(name = "emplacement_id")
    private UUID emplacementId;

    @Column(name = "cree_par")
    private UUID creePar;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "motif_override")
    private String motifOverride;

    protected DispensationJpaEntity() {
    }

    public static DispensationJpaEntity create(UUID id, UUID organisationId, UUID ordonnanceId, UUID ordonnanceLigneId, UUID produitId, int quantite,
                                               UUID lotId, UUID emplacementId, UUID creePar, String motifOverride, Instant now) {
        DispensationJpaEntity d = new DispensationJpaEntity();
        d.id = id;
        d.organisationId = organisationId;
        d.ordonnanceId = ordonnanceId;
        d.ordonnanceLigneId = ordonnanceLigneId;
        d.produitId = produitId;
        d.quantite = quantite;
        d.lotId = lotId;
        d.emplacementId = emplacementId;
        d.creePar = creePar;
        d.motifOverride = motifOverride;
        d.createdAt = now;
        return d;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrdonnanceLigneId() {
        return ordonnanceLigneId;
    }

    public UUID getProduitId() {
        return produitId;
    }

    public int getQuantite() {
        return quantite;
    }

    public UUID getLotId() {
        return lotId;
    }

    public UUID getEmplacementId() {
        return emplacementId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getMotifOverride() {
        return motifOverride;
    }
}

