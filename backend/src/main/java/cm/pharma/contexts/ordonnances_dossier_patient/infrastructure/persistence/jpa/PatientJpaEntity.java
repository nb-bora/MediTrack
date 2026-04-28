package cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "patient")
public class PatientJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "sexe", length = 20)
    private String sexe;

    @Column(name = "telephone", length = 30)
    private String telephone;

    @Column(name = "adresse")
    private String adresse;

    @Column(name = "assurance_organisme_nom", length = 120)
    private String assuranceOrganismeNom;

    @Column(name = "assurance_numero_adherent", length = 60)
    private String assuranceNumeroAdherent;

    @Column(name = "assurance_taux_couverture", precision = 5, scale = 2)
    private BigDecimal assuranceTauxCouverture;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PatientJpaEntity() {
    }

    public record PatientInit(
            UUID id,
            UUID organisationId,
            String nom,
            String prenom,
            LocalDate dateNaissance,
            String sexe,
            String telephone,
            String adresse,
            String assuranceOrganismeNom,
            String assuranceNumeroAdherent,
            BigDecimal assuranceTauxCouverture,
            Instant now
    ) {
    }

    public static PatientJpaEntity create(PatientInit init) {
        PatientJpaEntity p = new PatientJpaEntity();
        p.id = init.id();
        p.organisationId = init.organisationId();
        p.nom = init.nom();
        p.prenom = init.prenom();
        p.dateNaissance = init.dateNaissance();
        p.sexe = init.sexe();
        p.telephone = init.telephone();
        p.adresse = init.adresse();
        p.assuranceOrganismeNom = init.assuranceOrganismeNom();
        p.assuranceNumeroAdherent = init.assuranceNumeroAdherent();
        p.assuranceTauxCouverture = init.assuranceTauxCouverture();
        p.createdAt = init.now();
        p.updatedAt = init.now();
        return p;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getTelephone() {
        return telephone;
    }

    public java.time.LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public String getSexe() {
        return sexe;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getAssuranceOrganismeNom() {
        return assuranceOrganismeNom;
    }

    public String getAssuranceNumeroAdherent() {
        return assuranceNumeroAdherent;
    }

    public BigDecimal getAssuranceTauxCouverture() {
        return assuranceTauxCouverture;
    }
}

