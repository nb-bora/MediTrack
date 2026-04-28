package cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "organisme_assurance")
public class OrganismeAssuranceJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "code", nullable = false, length = 40)
    private String code;

    @Column(name = "nom", nullable = false, length = 180)
    private String nom;

    @Column(name = "type", nullable = false, length = 60)
    private String type;

    @Column(name = "frequence_facturation", nullable = false, length = 20)
    private String frequenceFacturation;

    @Column(name = "delai_paiement_jours", nullable = false)
    private int delaiPaiementJours;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected OrganismeAssuranceJpaEntity() {
    }

    public record OrganismeInit(
            UUID id,
            UUID organisationId,
            String code,
            String nom,
            String type,
            String frequenceFacturation,
            int delaiPaiementJours,
            Instant now
    ) {
    }

    public static OrganismeAssuranceJpaEntity create(OrganismeInit init) {
        OrganismeAssuranceJpaEntity o = new OrganismeAssuranceJpaEntity();
        o.id = init.id();
        o.organisationId = init.organisationId();
        o.code = init.code();
        o.nom = init.nom();
        o.type = init.type();
        o.frequenceFacturation = init.frequenceFacturation();
        o.delaiPaiementJours = init.delaiPaiementJours();
        o.createdAt = init.now();
        o.updatedAt = init.now();
        return o;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public String getCode() {
        return code;
    }

    public String getNom() {
        return nom;
    }

    public String getType() {
        return type;
    }

    public String getFrequenceFacturation() {
        return frequenceFacturation;
    }

    public int getDelaiPaiementJours() {
        return delaiPaiementJours;
    }
}

