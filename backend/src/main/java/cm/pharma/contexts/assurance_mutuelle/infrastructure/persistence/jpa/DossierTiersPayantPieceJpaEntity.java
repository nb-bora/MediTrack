package cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dossier_tiers_payant_piece")
public class DossierTiersPayantPieceJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "dossier_id", nullable = false)
    private UUID dossierId;

    @Column(name = "type_piece", nullable = false, length = 40)
    private String typePiece;

    @Column(name = "fichier_nom", nullable = false)
    private String fichierNom;

    @Column(name = "contenu_type", length = 120)
    private String contenuType;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    protected DossierTiersPayantPieceJpaEntity() {
    }

    public static DossierTiersPayantPieceJpaEntity create(UUID id, UUID organisationId, UUID dossierId, String typePiece, String fichierNom, String contenuType, String storageKey, UUID createdBy, Instant now) {
        DossierTiersPayantPieceJpaEntity p = new DossierTiersPayantPieceJpaEntity();
        p.id = id;
        p.organisationId = organisationId;
        p.dossierId = dossierId;
        p.typePiece = typePiece;
        p.fichierNom = fichierNom;
        p.contenuType = contenuType;
        p.storageKey = storageKey;
        p.createdBy = createdBy;
        p.createdAt = now;
        return p;
    }

    public UUID getId() {
        return id;
    }

    public UUID getDossierId() {
        return dossierId;
    }

    public String getTypePiece() {
        return typePiece;
    }

    public String getFichierNom() {
        return fichierNom;
    }

    public String getContenuType() {
        return contenuType;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

