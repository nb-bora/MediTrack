package cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "ordonnance_ligne")
public class OrdonnanceLigneJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "ordonnance_id", nullable = false)
    private UUID ordonnanceId;

    @Column(name = "produit_id", nullable = false)
    private UUID produitId;

    @Column(name = "quantite_prescrite", nullable = false)
    private int quantitePrescrite;

    @Column(name = "posologie")
    private String posologie;

    @Column(name = "duree_jours")
    private Integer dureeJours;

    @Column(name = "quantite_dispensee", nullable = false)
    private int quantiteDispensee;

    protected OrdonnanceLigneJpaEntity() {
    }

    public record OrdonnanceLigneInit(
            UUID id,
            UUID organisationId,
            UUID ordonnanceId,
            UUID produitId,
            int quantitePrescrite,
            String posologie,
            Integer dureeJours
    ) {
    }

    public static OrdonnanceLigneJpaEntity create(OrdonnanceLigneInit init) {
        OrdonnanceLigneJpaEntity l = new OrdonnanceLigneJpaEntity();
        l.id = init.id();
        l.organisationId = init.organisationId();
        l.ordonnanceId = init.ordonnanceId();
        l.produitId = init.produitId();
        l.quantitePrescrite = init.quantitePrescrite();
        l.posologie = init.posologie();
        l.dureeJours = init.dureeJours();
        l.quantiteDispensee = 0;
        return l;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrdonnanceId() {
        return ordonnanceId;
    }

    public UUID getProduitId() {
        return produitId;
    }

    public int getQuantitePrescrite() {
        return quantitePrescrite;
    }

    public String getPosologie() {
        return posologie;
    }

    public Integer getDureeJours() {
        return dureeJours;
    }

    public int getQuantiteDispensee() {
        return quantiteDispensee;
    }

    public void incrementerDispensee(int delta) {
        this.quantiteDispensee += delta;
        if (this.quantiteDispensee < 0) {
            this.quantiteDispensee = 0;
        }
        if (this.quantiteDispensee > this.quantitePrescrite) {
            this.quantiteDispensee = this.quantitePrescrite;
        }
    }
}

