package cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ordonnance_piece")
public class OrdonnancePieceJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "ordonnance_id", nullable = false)
    private UUID ordonnanceId;

    @Column(name = "fichier_nom", nullable = false)
    private String fichierNom;

    @Column(name = "contenu_type", length = 120)
    private String contenuType;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OrdonnancePieceJpaEntity() {
    }

    public static OrdonnancePieceJpaEntity create(UUID id, UUID organisationId, UUID ordonnanceId, String fichierNom, String contenuType, String storageKey, Instant now) {
        OrdonnancePieceJpaEntity p = new OrdonnancePieceJpaEntity();
        p.id = id;
        p.organisationId = organisationId;
        p.ordonnanceId = ordonnanceId;
        p.fichierNom = fichierNom;
        p.contenuType = contenuType;
        p.storageKey = storageKey;
        p.createdAt = now;
        return p;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrdonnanceId() {
        return ordonnanceId;
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

