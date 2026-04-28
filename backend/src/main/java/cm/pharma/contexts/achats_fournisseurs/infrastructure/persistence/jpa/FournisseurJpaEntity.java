package cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fournisseur")
public class FournisseurJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "raison_sociale", nullable = false)
    private String raisonSociale;

    @Column(name = "numero_rc")
    private String numeroRc;

    @Column(name = "numero_contribuable")
    private String numeroContribuable;

    @Column(name = "adresse")
    private String adresse;

    @Column(name = "contact_nom")
    private String contactNom;

    @Column(name = "contact_telephone")
    private String contactTelephone;

    @Column(name = "email_commandes")
    private String emailCommandes;

    @Column(name = "mode_paiement_prefere")
    private String modePaiementPrefere;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    protected FournisseurJpaEntity() {
    }

    public static FournisseurJpaEntity create(UUID id, UUID organisationId, String raisonSociale, String numeroRc, String numeroContribuable,
                                             String adresse, String contactNom, String contactTelephone, String emailCommandes,
                                             String modePaiementPrefere, UUID actorId, Instant now) {
        FournisseurJpaEntity f = new FournisseurJpaEntity();
        f.id = id;
        f.organisationId = organisationId;
        f.raisonSociale = raisonSociale;
        f.numeroRc = numeroRc;
        f.numeroContribuable = numeroContribuable;
        f.adresse = adresse;
        f.contactNom = contactNom;
        f.contactTelephone = contactTelephone;
        f.emailCommandes = emailCommandes;
        f.modePaiementPrefere = modePaiementPrefere;
        f.actif = true;
        f.createdAt = now;
        f.createdBy = actorId;
        f.updatedAt = now;
        f.updatedBy = actorId;
        return f;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public String getRaisonSociale() {
        return raisonSociale;
    }

    public String getNumeroRc() {
        return numeroRc;
    }

    public String getNumeroContribuable() {
        return numeroContribuable;
    }

    public boolean isActif() {
        return actif;
    }

    public void update(String raisonSociale, String numeroRc, String numeroContribuable, String adresse, String contactNom,
                       String contactTelephone, String emailCommandes, String modePaiementPrefere, UUID actorId, Instant now) {
        this.raisonSociale = raisonSociale;
        this.numeroRc = numeroRc;
        this.numeroContribuable = numeroContribuable;
        this.adresse = adresse;
        this.contactNom = contactNom;
        this.contactTelephone = contactTelephone;
        this.emailCommandes = emailCommandes;
        this.modePaiementPrefere = modePaiementPrefere;
        this.updatedBy = actorId;
        this.updatedAt = now;
    }

    public void setActif(boolean actif, UUID actorId, Instant now) {
        this.actif = actif;
        this.updatedBy = actorId;
        this.updatedAt = now;
    }
}

