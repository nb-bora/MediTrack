package cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "produit")
public class ProduitJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "type_produit", nullable = false, length = 20)
    private String typeProduit;

    @Column(name = "dci")
    private String dci;

    @Column(name = "nom_commercial", nullable = false)
    private String nomCommercial;

    @Column(name = "forme_galenique")
    private String formeGalenique;

    @Column(name = "dosage")
    private String dosage;

    @Column(name = "laboratoire")
    private String laboratoire;

    @Column(name = "pays_origine")
    private String paysOrigine;

    @Column(name = "categorie_id")
    private UUID categorieId;

    @Column(name = "necessite_ordonnance", nullable = false)
    private boolean necessiteOrdonnance;

    @Column(name = "est_stupefiant", nullable = false)
    private boolean estStupefiant;

    @Column(name = "est_psychotrope", nullable = false)
    private boolean estPsychotrope;

    @Column(name = "est_controle", nullable = false)
    private boolean estControle;

    @Column(name = "profil_taxe_id", nullable = false)
    private UUID profilTaxeId;

    @Column(name = "stock_minimum")
    private Integer stockMinimum;

    @Column(name = "stock_securite")
    private Integer stockSecurite;

    @Column(name = "delai_reappro_jours")
    private Integer delaiReapproJours;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProduitJpaEntity() {
    }

    public record ProduitInit(
            UUID organisationId,
            String typeProduit,
            String dci,
            String nomCommercial,
            String formeGalenique,
            String dosage,
            String laboratoire,
            String paysOrigine,
            UUID categorieId,
            boolean necessiteOrdonnance,
            boolean estStupefiant,
            boolean estPsychotrope,
            boolean estControle,
            UUID profilTaxeId,
            Integer stockMinimum,
            Integer stockSecurite,
            Integer delaiReapproJours
    ) {
    }

    public static ProduitJpaEntity create(UUID id, ProduitInit init, Instant now) {
        ProduitJpaEntity p = new ProduitJpaEntity();
        p.id = id;
        p.organisationId = init.organisationId();
        p.typeProduit = init.typeProduit();
        p.dci = init.dci();
        p.nomCommercial = init.nomCommercial();
        p.formeGalenique = init.formeGalenique();
        p.dosage = init.dosage();
        p.laboratoire = init.laboratoire();
        p.paysOrigine = init.paysOrigine();
        p.categorieId = init.categorieId();
        p.necessiteOrdonnance = init.necessiteOrdonnance();
        p.estStupefiant = init.estStupefiant();
        p.estPsychotrope = init.estPsychotrope();
        p.estControle = init.estControle();
        p.profilTaxeId = init.profilTaxeId();
        p.stockMinimum = init.stockMinimum();
        p.stockSecurite = init.stockSecurite();
        p.delaiReapproJours = init.delaiReapproJours();
        p.actif = true;
        p.createdAt = now;
        p.updatedAt = now;
        return p;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public String getDci() {
        return dci;
    }

    public String getDosage() {
        return dosage;
    }

    public String getNomCommercial() {
        return nomCommercial;
    }

    public String getTypeProduit() {
        return typeProduit;
    }

    public boolean isNecessiteOrdonnance() {
        return necessiteOrdonnance;
    }
}

