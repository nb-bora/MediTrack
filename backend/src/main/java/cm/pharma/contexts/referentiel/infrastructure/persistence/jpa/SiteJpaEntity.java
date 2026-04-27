package cm.pharma.contexts.referentiel.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "site")
public class SiteJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "adresse", nullable = false)
    private String adresse;

    @Column(name = "telephone", length = 30)
    private String telephone;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SiteJpaEntity() {
    }

    public static SiteJpaEntity create(UUID id, UUID organisationId, String nom, String adresse, String telephone, Instant now) {
        SiteJpaEntity e = new SiteJpaEntity();
        e.id = id;
        e.organisationId = organisationId;
        e.nom = nom;
        e.adresse = adresse;
        e.telephone = telephone;
        e.actif = true;
        e.createdAt = now;
        e.updatedAt = now;
        return e;
    }

    public UUID getId() {
        return id;
    }
}

