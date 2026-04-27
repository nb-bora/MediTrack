package cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mouvement_stock")
public class MouvementStockJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "type_mouvement", nullable = false, length = 30)
    private String typeMouvement;

    @Column(name = "lot_id", nullable = false)
    private UUID lotId;

    @Column(name = "produit_id", nullable = false)
    private UUID produitId;

    @Column(name = "quantite", nullable = false)
    private int quantite;

    @Column(name = "emplacement_source_id")
    private UUID emplacementSourceId;

    @Column(name = "emplacement_destination_id")
    private UUID emplacementDestinationId;

    @Column(name = "reference_document")
    private String referenceDocument;

    @Column(name = "motif")
    private String motif;

    @Column(name = "cree_par")
    private UUID creePar;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected MouvementStockJpaEntity() {
    }

    public record MouvementInit(
            UUID id,
            UUID organisationId,
            String typeMouvement,
            UUID lotId,
            UUID produitId,
            int quantite,
            UUID emplacementSourceId,
            UUID emplacementDestinationId,
            String referenceDocument,
            String motif,
            UUID creePar,
            Instant now
    ) {
    }

    public static MouvementStockJpaEntity create(MouvementInit init) {
        MouvementStockJpaEntity m = new MouvementStockJpaEntity();
        m.id = init.id();
        m.organisationId = init.organisationId();
        m.typeMouvement = init.typeMouvement();
        m.lotId = init.lotId();
        m.produitId = init.produitId();
        m.quantite = init.quantite();
        m.emplacementSourceId = init.emplacementSourceId();
        m.emplacementDestinationId = init.emplacementDestinationId();
        m.referenceDocument = init.referenceDocument();
        m.motif = init.motif();
        m.creePar = init.creePar();
        m.createdAt = init.now();
        return m;
    }

    public UUID getId() {
        return id;
    }

    public String getTypeMouvement() {
        return typeMouvement;
    }

    public UUID getLotId() {
        return lotId;
    }

    public UUID getProduitId() {
        return produitId;
    }

    public int getQuantite() {
        return quantite;
    }

    public UUID getEmplacementSourceId() {
        return emplacementSourceId;
    }

    public UUID getEmplacementDestinationId() {
        return emplacementDestinationId;
    }

    public String getReferenceDocument() {
        return referenceDocument;
    }

    public String getMotif() {
        return motif;
    }

    public UUID getCreePar() {
        return creePar;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

