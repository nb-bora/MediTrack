package cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "retour_vente")
public class RetourVenteJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "vente_id", nullable = false)
    private UUID venteId;

    @Column(name = "numero_retour", nullable = false, length = 40)
    private String numeroRetour;

    @Column(name = "motif", nullable = false)
    private String motif;

    @Column(name = "mode_remboursement", nullable = false, length = 30)
    private String modeRemboursement;

    @Column(name = "reference")
    private String reference;

    @Column(name = "montant_rembourse", nullable = false, precision = 19, scale = 4)
    private BigDecimal montantRembourse;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    protected RetourVenteJpaEntity() {
    }

    public static RetourVenteJpaEntity create(UUID id, UUID organisationId, UUID venteId, String numeroRetour, String motif,
                                              String modeRemboursement, String reference, BigDecimal montantRembourse, UUID createdBy, Instant now) {
        RetourVenteJpaEntity r = new RetourVenteJpaEntity();
        r.id = id;
        r.organisationId = organisationId;
        r.venteId = venteId;
        r.numeroRetour = numeroRetour;
        r.motif = motif;
        r.modeRemboursement = modeRemboursement;
        r.reference = reference;
        r.montantRembourse = montantRembourse == null ? BigDecimal.ZERO : montantRembourse;
        r.createdAt = now;
        r.createdBy = createdBy;
        return r;
    }

    public UUID getId() {
        return id;
    }

    public String getNumeroRetour() {
        return numeroRetour;
    }
}

