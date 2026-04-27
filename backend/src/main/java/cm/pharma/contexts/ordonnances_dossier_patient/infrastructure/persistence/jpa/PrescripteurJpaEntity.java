package cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prescripteur")
public class PrescripteurJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "nom", nullable = false, length = 180)
    private String nom;

    @Column(name = "structure", length = 180)
    private String structure;

    @Column(name = "telephone", length = 30)
    private String telephone;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PrescripteurJpaEntity() {
    }

    public record PrescripteurInit(UUID id, UUID organisationId, String nom, String structure, String telephone, Instant now) {
    }

    public static PrescripteurJpaEntity create(PrescripteurInit init) {
        PrescripteurJpaEntity p = new PrescripteurJpaEntity();
        p.id = init.id();
        p.organisationId = init.organisationId();
        p.nom = init.nom();
        p.structure = init.structure();
        p.telephone = init.telephone();
        p.createdAt = init.now();
        return p;
    }

    public UUID getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getStructure() {
        return structure;
    }

    public String getTelephone() {
        return telephone;
    }
}

