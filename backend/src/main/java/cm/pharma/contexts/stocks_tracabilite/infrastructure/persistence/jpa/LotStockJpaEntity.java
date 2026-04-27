package cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "lot_stock")
public class LotStockJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "produit_id", nullable = false)
    private UUID produitId;

    @Column(name = "numero_lot", nullable = false)
    private String numeroLot;

    @Column(name = "date_peremption", nullable = false)
    private LocalDate datePeremption;

    @Column(name = "statut", nullable = false, length = 30)
    private String statut;

    @Column(name = "motif_statut")
    private String motifStatut;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    protected LotStockJpaEntity() {
    }

    public static LotStockJpaEntity create(UUID id, UUID organisationId, UUID produitId, String numeroLot, LocalDate datePeremption,
                                          String statut, String motifStatut, UUID createdBy, Instant now) {
        LotStockJpaEntity e = new LotStockJpaEntity();
        e.id = id;
        e.organisationId = organisationId;
        e.produitId = produitId;
        e.numeroLot = numeroLot;
        e.datePeremption = datePeremption;
        e.statut = statut;
        e.motifStatut = motifStatut;
        e.createdAt = now;
        e.createdBy = createdBy;
        e.updatedAt = now;
        e.updatedBy = createdBy;
        return e;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public UUID getProduitId() {
        return produitId;
    }

    public String getNumeroLot() {
        return numeroLot;
    }

    public LocalDate getDatePeremption() {
        return datePeremption;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut, String motif, UUID updatedBy, Instant now) {
        this.statut = statut;
        this.motifStatut = motif;
        this.updatedBy = updatedBy;
        this.updatedAt = now;
    }
}

