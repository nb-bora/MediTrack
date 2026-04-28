package cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "bon_commande_ligne")
public class BonCommandeLigneJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "bon_commande_id", nullable = false)
    private UUID bonCommandeId;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "produit_id", nullable = false)
    private UUID produitId;

    @Column(name = "quantite_commandee", nullable = false)
    private int quantiteCommandee;

    @Column(name = "quantite_recue", nullable = false)
    private int quantiteRecue;

    @Column(name = "prix_attendu_unitaire", precision = 19, scale = 4)
    private BigDecimal prixAttenduUnitaire;

    @Column(name = "devise", nullable = false, length = 3)
    private String devise;

    protected BonCommandeLigneJpaEntity() {
    }

    public static BonCommandeLigneJpaEntity create(UUID id, UUID bonCommandeId, UUID organisationId, UUID produitId, int quantiteCommandee,
                                                  BigDecimal prixAttenduUnitaire, String devise) {
        BonCommandeLigneJpaEntity l = new BonCommandeLigneJpaEntity();
        l.id = id;
        l.bonCommandeId = bonCommandeId;
        l.organisationId = organisationId;
        l.produitId = produitId;
        l.quantiteCommandee = quantiteCommandee;
        l.quantiteRecue = 0;
        l.prixAttenduUnitaire = prixAttenduUnitaire;
        l.devise = devise;
        return l;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBonCommandeId() {
        return bonCommandeId;
    }

    public UUID getProduitId() {
        return produitId;
    }

    public int getQuantiteCommandee() {
        return quantiteCommandee;
    }

    public int getQuantiteRecue() {
        return quantiteRecue;
    }

    public BigDecimal getPrixAttenduUnitaire() {
        return prixAttenduUnitaire;
    }

    public String getDevise() {
        return devise;
    }

    public void incrementerQuantiteRecue(int delta) {
        this.quantiteRecue += delta;
    }
}

