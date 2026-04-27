package cm.pharma.contexts.referentiel.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "organisation")
public class OrganisationJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "nom_commercial", nullable = false)
    private String nomCommercial;

    @Column(name = "numero_autorisation_ouverture", nullable = false, length = 50)
    private String numeroAutorisationOuverture;

    @Column(name = "adresse", nullable = false)
    private String adresse;

    @Column(name = "telephone", nullable = false, length = 30)
    private String telephone;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "responsable_legal_nom", nullable = false)
    private String responsableLegalNom;

    @Column(name = "responsable_legal_numero_ordre", length = 50)
    private String responsableLegalNumeroOrdre;

    @Column(name = "devise", nullable = false, length = 3)
    private String devise;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected OrganisationJpaEntity() {
    }

    public static OrganisationJpaEntity create(UUID id, OrganisationInit init, Instant now) {
        OrganisationJpaEntity e = new OrganisationJpaEntity();
        e.id = id;
        e.nomCommercial = init.nomCommercial();
        e.numeroAutorisationOuverture = init.numeroAutorisationOuverture();
        e.adresse = init.adresse();
        e.telephone = init.telephone();
        e.email = init.email();
        e.responsableLegalNom = init.responsableLegalNom();
        e.responsableLegalNumeroOrdre = init.responsableLegalNumeroOrdre();
        e.devise = init.devise();
        e.actif = true;
        e.createdAt = now;
        e.updatedAt = now;
        return e;
    }

    public record OrganisationInit(
            String nomCommercial,
            String numeroAutorisationOuverture,
            String adresse,
            String telephone,
            String email,
            String responsableLegalNom,
            String responsableLegalNumeroOrdre,
            String devise
    ) {
    }

    public UUID getId() {
        return id;
    }
}

