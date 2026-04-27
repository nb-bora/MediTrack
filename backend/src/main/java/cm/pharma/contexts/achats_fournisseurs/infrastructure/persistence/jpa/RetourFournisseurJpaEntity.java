package cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "retour_fournisseur")
public class RetourFournisseurJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "fournisseur_id", nullable = false)
    private UUID fournisseurId;

    @Column(name = "statut", nullable = false, length = 20)
    private String statut;

    @Column(name = "motif", nullable = false)
    private String motif;

    @Column(name = "reference_document")
    private String referenceDocument;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "validated_at")
    private Instant validatedAt;

    @Column(name = "validated_by")
    private UUID validatedBy;

    protected RetourFournisseurJpaEntity() {
    }

    public static RetourFournisseurJpaEntity create(UUID id, UUID organisationId, UUID fournisseurId, String motif, String referenceDocument, UUID createdBy, Instant now) {
        RetourFournisseurJpaEntity r = new RetourFournisseurJpaEntity();
        r.id = id;
        r.organisationId = organisationId;
        r.fournisseurId = fournisseurId;
        r.statut = "BROUILLON";
        r.motif = motif;
        r.referenceDocument = referenceDocument;
        r.createdAt = now;
        r.createdBy = createdBy;
        return r;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public UUID getFournisseurId() {
        return fournisseurId;
    }

    public String getStatut() {
        return statut;
    }

    public String getMotif() {
        return motif;
    }

    public void valider(UUID actorId, Instant now) {
        this.statut = "VALIDE";
        this.validatedBy = actorId;
        this.validatedAt = now;
    }

    public void marquerEnvoye() {
        this.statut = "ENVOYE";
    }

    public void cloturer() {
        this.statut = "CLOTURE";
    }
}

