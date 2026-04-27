package cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vente")
public class VenteJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "session_caisse_id", nullable = false)
    private UUID sessionCaisseId;

    @Column(name = "numero_vente", nullable = false, length = 40)
    private String numeroVente;

    @Column(name = "numero_ticket", nullable = false, length = 40)
    private String numeroTicket;

    @Column(name = "statut", nullable = false, length = 20)
    private String statut;

    @Column(name = "total_ttc", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalTtc;

    @Column(name = "total_remise", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalRemise;

    @Column(name = "arrondi", nullable = false, precision = 19, scale = 4)
    private BigDecimal arrondi;

    @Column(name = "devise", nullable = false, length = 3)
    private String devise;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "validated_at")
    private Instant validatedAt;

    @Column(name = "validated_by")
    private UUID validatedBy;

    @Column(name = "annulee_at")
    private Instant annuleeAt;

    @Column(name = "annulee_by")
    private UUID annuleeBy;

    @Column(name = "motif_annulation")
    private String motifAnnulation;

    protected VenteJpaEntity() {
    }

    public record VenteInit(
            UUID id,
            UUID organisationId,
            UUID sessionCaisseId,
            String numeroVente,
            String numeroTicket,
            UUID createdBy,
            String devise,
            Instant now
    ) {
    }

    public static VenteJpaEntity create(VenteInit init) {
        VenteJpaEntity v = new VenteJpaEntity();
        v.id = init.id();
        v.organisationId = init.organisationId();
        v.sessionCaisseId = init.sessionCaisseId();
        v.numeroVente = init.numeroVente();
        v.numeroTicket = init.numeroTicket();
        v.statut = "BROUILLON";
        v.totalTtc = BigDecimal.ZERO;
        v.totalRemise = BigDecimal.ZERO;
        v.arrondi = BigDecimal.ZERO;
        v.devise = init.devise();
        v.createdAt = init.now();
        v.createdBy = init.createdBy();
        return v;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public String getNumeroVente() {
        return numeroVente;
    }

    public String getNumeroTicket() {
        return numeroTicket;
    }

    public String getStatut() {
        return statut;
    }

    public BigDecimal getTotalTtc() {
        return totalTtc;
    }

    public BigDecimal getArrondi() {
        return arrondi;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setTotals(BigDecimal totalTtc, BigDecimal totalRemise) {
        this.totalTtc = totalTtc;
        this.totalRemise = totalRemise;
    }

    public void setArrondi(BigDecimal arrondi) {
        this.arrondi = arrondi == null ? BigDecimal.ZERO : arrondi;
        if (this.arrondi.compareTo(BigDecimal.ZERO) < 0) {
            this.arrondi = BigDecimal.ZERO;
        }
    }

    public void valider(UUID validatedBy, Instant now) {
        this.statut = "VALIDEE";
        this.validatedBy = validatedBy;
        this.validatedAt = now;
    }

    public void annuler(UUID actorId, String motif, Instant now) {
        this.statut = "ANNULEE";
        this.annuleeBy = actorId;
        this.annuleeAt = now;
        this.motifAnnulation = motif;
    }
}

