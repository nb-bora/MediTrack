package cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventaire_ligne")
public class InventaireLigneJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "inventaire_id", nullable = false)
    private UUID inventaireId;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "produit_id", nullable = false)
    private UUID produitId;

    @Column(name = "emplacement_id", nullable = false)
    private UUID emplacementId;

    @Column(name = "stock_theorique", nullable = false)
    private int stockTheorique;

    @Column(name = "stock_reel")
    private Integer stockReel;

    @Column(name = "ecart")
    private Integer ecart;

    @Column(name = "motif_ecart")
    private String motifEcart;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected InventaireLigneJpaEntity() {
    }

    public static InventaireLigneJpaEntity create(UUID id, UUID inventaireId, UUID organisationId, UUID produitId, UUID emplacementId,
                                                  int stockTheorique, Instant now) {
        InventaireLigneJpaEntity l = new InventaireLigneJpaEntity();
        l.id = id;
        l.inventaireId = inventaireId;
        l.organisationId = organisationId;
        l.produitId = produitId;
        l.emplacementId = emplacementId;
        l.stockTheorique = stockTheorique;
        l.updatedAt = now;
        return l;
    }

    public UUID getId() {
        return id;
    }

    public UUID getInventaireId() {
        return inventaireId;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public UUID getProduitId() {
        return produitId;
    }

    public UUID getEmplacementId() {
        return emplacementId;
    }

    public int getStockTheorique() {
        return stockTheorique;
    }

    public Integer getStockReel() {
        return stockReel;
    }

    public Integer getEcart() {
        return ecart;
    }

    public String getMotifEcart() {
        return motifEcart;
    }

    public void saisirStockReel(int stockReel, String motifEcart, Instant now) {
        this.stockReel = stockReel;
        this.ecart = stockReel - stockTheorique;
        this.motifEcart = motifEcart;
        this.updatedAt = now;
    }
}

