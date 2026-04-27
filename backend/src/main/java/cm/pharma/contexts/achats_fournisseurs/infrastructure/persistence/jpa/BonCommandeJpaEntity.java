package cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "bon_commande")
public class BonCommandeJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "numero", nullable = false, length = 40)
    private String numero;

    @Column(name = "fournisseur_id", nullable = false)
    private UUID fournisseurId;

    @Column(name = "statut", nullable = false, length = 20)
    private String statut;

    @Column(name = "date_commande", nullable = false)
    private LocalDate dateCommande;

    @Column(name = "date_livraison_prevue")
    private LocalDate dateLivraisonPrevue;

    @Column(name = "commentaire")
    private String commentaire;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "validated_at")
    private Instant validatedAt;

    @Column(name = "validated_by")
    private UUID validatedBy;

    protected BonCommandeJpaEntity() {
    }

    public record BonCommandeInit(
            UUID id,
            UUID organisationId,
            String numero,
            UUID fournisseurId,
            LocalDate dateCommande,
            LocalDate dateLivraisonPrevue,
            String commentaire,
            UUID createdBy,
            Instant now
    ) {
    }

    public static BonCommandeJpaEntity create(BonCommandeInit init) {
        BonCommandeJpaEntity bc = new BonCommandeJpaEntity();
        bc.id = init.id();
        bc.organisationId = init.organisationId();
        bc.numero = init.numero();
        bc.fournisseurId = init.fournisseurId();
        bc.statut = "BROUILLON";
        bc.dateCommande = init.dateCommande();
        bc.dateLivraisonPrevue = init.dateLivraisonPrevue();
        bc.commentaire = init.commentaire();
        bc.createdAt = init.now();
        bc.createdBy = init.createdBy();
        return bc;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public String getNumero() {
        return numero;
    }

    public UUID getFournisseurId() {
        return fournisseurId;
    }

    public String getStatut() {
        return statut;
    }

    public LocalDate getDateLivraisonPrevue() {
        return dateLivraisonPrevue;
    }

    public void valider(UUID actorId, Instant now) {
        this.statut = "VALIDE";
        this.validatedBy = actorId;
        this.validatedAt = now;
    }

    public void marquerRecuPartiel() {
        this.statut = "RECU_PARTIEL";
    }

    public void marquerRecuComplet() {
        this.statut = "RECU_COMPLET";
    }
}

