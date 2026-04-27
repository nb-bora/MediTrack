package cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_emplacement")
public class StockEmplacementJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "emplacement_id", nullable = false)
    private UUID emplacementId;

    @Column(name = "lot_id", nullable = false)
    private UUID lotId;

    @Column(name = "quantite", nullable = false)
    private int quantite;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected StockEmplacementJpaEntity() {
    }

    public static StockEmplacementJpaEntity create(UUID id, UUID organisationId, UUID emplacementId, UUID lotId, int quantite, Instant now) {
        StockEmplacementJpaEntity s = new StockEmplacementJpaEntity();
        s.id = id;
        s.organisationId = organisationId;
        s.emplacementId = emplacementId;
        s.lotId = lotId;
        s.quantite = quantite;
        s.updatedAt = now;
        return s;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public UUID getEmplacementId() {
        return emplacementId;
    }

    public UUID getLotId() {
        return lotId;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite, Instant now) {
        this.quantite = quantite;
        this.updatedAt = now;
    }
}

