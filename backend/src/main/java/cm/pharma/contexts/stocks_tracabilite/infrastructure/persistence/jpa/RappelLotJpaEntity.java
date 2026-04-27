package cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rappel_lot")
public class RappelLotJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "produit_id", nullable = false)
    private UUID produitId;

    @Column(name = "lot_id", nullable = false)
    private UUID lotId;

    @Column(name = "criticite", nullable = false, length = 20)
    private String criticite;

    @Column(name = "motif", nullable = false)
    private String motif;

    @Column(name = "source")
    private String source;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    protected RappelLotJpaEntity() {
    }

    public static RappelLotJpaEntity create(UUID id, UUID organisationId, UUID produitId, UUID lotId, String criticite,
                                           String motif, String source, UUID createdBy, Instant now) {
        RappelLotJpaEntity r = new RappelLotJpaEntity();
        r.id = id;
        r.organisationId = organisationId;
        r.produitId = produitId;
        r.lotId = lotId;
        r.criticite = criticite;
        r.motif = motif;
        r.source = source;
        r.createdAt = now;
        r.createdBy = createdBy;
        return r;
    }
}

