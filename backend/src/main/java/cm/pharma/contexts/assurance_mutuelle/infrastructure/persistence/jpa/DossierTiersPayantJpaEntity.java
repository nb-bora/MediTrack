package cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dossier_tiers_payant")
public class DossierTiersPayantJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "organisme_id", nullable = false)
    private UUID organismeId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "vente_id")
    private UUID venteId;

    @Column(name = "ordonnance_id")
    private UUID ordonnanceId;

    @Column(name = "numero_dossier", nullable = false, length = 40)
    private String numeroDossier;

    @Column(name = "statut", nullable = false, length = 30)
    private String statut;

    @Column(name = "taux_couverture", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxCouverture;

    @Column(name = "montant_total", nullable = false, precision = 19, scale = 4)
    private BigDecimal montantTotal;

    @Column(name = "montant_prise_en_charge", nullable = false, precision = 19, scale = 4)
    private BigDecimal montantPriseEnCharge;

    @Column(name = "montant_reste_patient", nullable = false, precision = 19, scale = 4)
    private BigDecimal montantRestePatient;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "soumis_at")
    private Instant soumisAt;

    @Column(name = "soumis_by")
    private UUID soumisBy;

    @Column(name = "rejete_at")
    private Instant rejeteAt;

    @Column(name = "rejete_by")
    private UUID rejeteBy;

    @Column(name = "motif_rejet")
    private String motifRejet;

    @Column(name = "resoumis_at")
    private Instant resoumisAt;

    @Column(name = "paye_at")
    private Instant payeAt;

    @Column(name = "reference_paiement")
    private String referencePaiement;

    protected DossierTiersPayantJpaEntity() {
    }

    public record DossierInit(
            UUID id,
            UUID organisationId,
            UUID organismeId,
            UUID patientId,
            UUID venteId,
            UUID ordonnanceId,
            String numeroDossier,
            BigDecimal tauxCouverture,
            BigDecimal montantTotal,
            BigDecimal montantPriseEnCharge,
            BigDecimal montantRestePatient,
            UUID createdBy,
            Instant now
    ) {
    }

    public static DossierTiersPayantJpaEntity create(DossierInit init) {
        DossierTiersPayantJpaEntity d = new DossierTiersPayantJpaEntity();
        d.id = init.id();
        d.organisationId = init.organisationId();
        d.organismeId = init.organismeId();
        d.patientId = init.patientId();
        d.venteId = init.venteId();
        d.ordonnanceId = init.ordonnanceId();
        d.numeroDossier = init.numeroDossier();
        d.statut = "BROUILLON";
        d.tauxCouverture = init.tauxCouverture();
        d.montantTotal = init.montantTotal();
        d.montantPriseEnCharge = init.montantPriseEnCharge();
        d.montantRestePatient = init.montantRestePatient();
        d.createdBy = init.createdBy();
        d.createdAt = init.now();
        return d;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public UUID getOrganismeId() {
        return organismeId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public UUID getVenteId() {
        return venteId;
    }

    public UUID getOrdonnanceId() {
        return ordonnanceId;
    }

    public String getNumeroDossier() {
        return numeroDossier;
    }

    public String getStatut() {
        return statut;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public BigDecimal getMontantPriseEnCharge() {
        return montantPriseEnCharge;
    }

    public BigDecimal getMontantRestePatient() {
        return montantRestePatient;
    }

    public void soumettre(UUID actorId, Instant now) {
        if (!"BROUILLON".equals(this.statut) && !"REJETE".equals(this.statut)) {
            throw new IllegalStateException("Statut dossier invalide pour soumission");
        }
        this.statut = "SOUMIS";
        this.soumisBy = actorId;
        this.soumisAt = now;
        this.motifRejet = null;
    }

    public void rejeter(UUID actorId, String motif, Instant now) {
        this.statut = "REJETE";
        this.rejeteBy = actorId;
        this.rejeteAt = now;
        this.motifRejet = motif;
    }

    public void resoumettre(UUID actorId, Instant now) {
        this.statut = "RESOUMIS";
        this.resoumisAt = now;
        this.soumisBy = actorId;
        this.soumisAt = now;
        this.motifRejet = null;
    }

    public void marquerPaye(String reference, Instant now) {
        this.statut = "PAYE";
        this.referencePaiement = reference;
        this.payeAt = now;
    }
}

