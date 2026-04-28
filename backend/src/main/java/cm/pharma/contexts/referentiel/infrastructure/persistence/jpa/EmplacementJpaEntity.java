package cm.pharma.contexts.referentiel.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "emplacement")
public class EmplacementJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "site_id", nullable = false)
    private UUID siteId;

    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "type_emplacement", nullable = false, length = 30)
    private String typeEmplacement;

    @Column(name = "ordre_affichage", nullable = false)
    private int ordreAffichage;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected EmplacementJpaEntity() {
    }

    public static EmplacementJpaEntity create(UUID id, UUID siteId, String code, String nom, String type, int ordre, Instant now) {
        EmplacementJpaEntity e = new EmplacementJpaEntity();
        e.id = id;
        e.siteId = siteId;
        e.code = code;
        e.nom = nom;
        e.typeEmplacement = type;
        e.ordreAffichage = ordre;
        e.actif = true;
        e.createdAt = now;
        e.updatedAt = now;
        return e;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSiteId() {
        return siteId;
    }

    public String getCode() {
        return code;
    }

    public String getNom() {
        return nom;
    }

    public String getTypeEmplacement() {
        return typeEmplacement;
    }

    public int getOrdreAffichage() {
        return ordreAffichage;
    }

    public boolean isActif() {
        return actif;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(String code, String nom, String type, int ordre, boolean actif, Instant now) {
        this.code = Objects.requireNonNull(code);
        this.nom = Objects.requireNonNull(nom);
        this.typeEmplacement = Objects.requireNonNull(type);
        this.ordreAffichage = ordre;
        this.actif = actif;
        this.updatedAt = now;
    }
}

