package cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reception_fournisseur")
public class ReceptionFournisseurJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "bon_commande_id")
    private UUID bonCommandeId;

    @Column(name = "fournisseur_id", nullable = false)
    private UUID fournisseurId;

    @Column(name = "reference_document")
    private String referenceDocument;

    @Column(name = "statut_validation_prix", nullable = false, length = 20)
    private String statutValidationPrix;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    protected ReceptionFournisseurJpaEntity() {
    }

    public static ReceptionFournisseurJpaEntity create(UUID id, UUID organisationId, UUID bonCommandeId, UUID fournisseurId,
                                                       String referenceDocument, UUID createdBy, Instant now) {
        ReceptionFournisseurJpaEntity r = new ReceptionFournisseurJpaEntity();
        r.id = id;
        r.organisationId = organisationId;
        r.bonCommandeId = bonCommandeId;
        r.fournisseurId = fournisseurId;
        r.referenceDocument = referenceDocument;
        r.statutValidationPrix = "OK";
        r.createdAt = now;
        r.createdBy = createdBy;
        return r;
    }

    public UUID getId() {
        return id;
    }

    public String getStatutValidationPrix() {
        return statutValidationPrix;
    }

    public void setStatutValidationPrix(String statutValidationPrix) {
        this.statutValidationPrix = statutValidationPrix;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }
}

