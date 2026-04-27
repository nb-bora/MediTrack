package cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "vente_ligne")
public class VenteLigneJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "vente_id", nullable = false)
    private UUID venteId;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "produit_id", nullable = false)
    private UUID produitId;

    @Column(name = "quantite", nullable = false)
    private int quantite;

    @Column(name = "prix_unitaire_ttc", nullable = false, precision = 19, scale = 4)
    private BigDecimal prixUnitaireTtc;

    @Column(name = "remise", nullable = false, precision = 19, scale = 4)
    private BigDecimal remise;

    @Column(name = "total_ligne", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalLigne;

    protected VenteLigneJpaEntity() {
    }

    public record VenteLigneInit(
            UUID id,
            UUID venteId,
            UUID organisationId,
            UUID produitId,
            int quantite,
            BigDecimal prixUnitaireTtc,
            BigDecimal remise,
            BigDecimal totalLigne
    ) {
    }

    public static VenteLigneJpaEntity create(VenteLigneInit init) {
        VenteLigneJpaEntity l = new VenteLigneJpaEntity();
        l.id = init.id();
        l.venteId = init.venteId();
        l.organisationId = init.organisationId();
        l.produitId = init.produitId();
        l.quantite = init.quantite();
        l.prixUnitaireTtc = init.prixUnitaireTtc();
        l.remise = init.remise();
        l.totalLigne = init.totalLigne();
        return l;
    }

    public UUID getId() {
        return id;
    }

    public UUID getVenteId() {
        return venteId;
    }

    public UUID getProduitId() {
        return produitId;
    }

    public int getQuantite() {
        return quantite;
    }

    public BigDecimal getTotalLigne() {
        return totalLigne;
    }

    public BigDecimal getPrixUnitaireTtc() {
        return prixUnitaireTtc;
    }

    public BigDecimal getRemise() {
        return remise;
    }

    public void appliquerRemise(BigDecimal remise) {
        this.remise = remise == null ? BigDecimal.ZERO : remise;
        if (this.remise.compareTo(BigDecimal.ZERO) < 0) {
            this.remise = BigDecimal.ZERO;
        }
        this.totalLigne = prixUnitaireTtc.multiply(BigDecimal.valueOf(this.quantite)).subtract(this.remise);
        if (this.totalLigne.compareTo(BigDecimal.ZERO) < 0) {
            this.totalLigne = BigDecimal.ZERO;
        }
    }

    public void incrementerQuantite(int delta, BigDecimal prixUnitaireTtc) {
        this.quantite += delta;
        this.prixUnitaireTtc = prixUnitaireTtc;
        this.totalLigne = prixUnitaireTtc.multiply(BigDecimal.valueOf(this.quantite)).subtract(remise);
        if (this.totalLigne.compareTo(BigDecimal.ZERO) < 0) {
            this.totalLigne = BigDecimal.ZERO;
        }
    }
}

