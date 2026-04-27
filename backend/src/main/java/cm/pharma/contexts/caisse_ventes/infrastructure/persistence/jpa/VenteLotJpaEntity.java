package cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "vente_lot")
public class VenteLotJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "vente_ligne_id", nullable = false)
    private UUID venteLigneId;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "lot_id", nullable = false)
    private UUID lotId;

    @Column(name = "quantite", nullable = false)
    private int quantite;

    @Column(name = "emplacement_id", nullable = false)
    private UUID emplacementId;

    protected VenteLotJpaEntity() {
    }

    public static VenteLotJpaEntity create(UUID id, UUID venteLigneId, UUID organisationId, UUID lotId, int quantite, UUID emplacementId) {
        VenteLotJpaEntity vl = new VenteLotJpaEntity();
        vl.id = id;
        vl.venteLigneId = venteLigneId;
        vl.organisationId = organisationId;
        vl.lotId = lotId;
        vl.quantite = quantite;
        vl.emplacementId = emplacementId;
        return vl;
    }

    public UUID getLotId() {
        return lotId;
    }

    public int getQuantite() {
        return quantite;
    }

    public UUID getEmplacementId() {
        return emplacementId;
    }
}

