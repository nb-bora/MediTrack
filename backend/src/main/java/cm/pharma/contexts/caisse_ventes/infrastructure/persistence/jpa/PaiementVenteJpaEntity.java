package cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "paiement_vente")
public class PaiementVenteJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "vente_id", nullable = false)
    private UUID venteId;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "mode_paiement", nullable = false, length = 30)
    private String modePaiement;

    @Column(name = "montant", nullable = false, precision = 19, scale = 4)
    private BigDecimal montant;

    @Column(name = "reference")
    private String reference;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PaiementVenteJpaEntity() {
    }

    public static PaiementVenteJpaEntity create(UUID id, UUID venteId, UUID organisationId, String modePaiement, BigDecimal montant,
                                                String reference, Instant now) {
        PaiementVenteJpaEntity p = new PaiementVenteJpaEntity();
        p.id = id;
        p.venteId = venteId;
        p.organisationId = organisationId;
        p.modePaiement = modePaiement;
        p.montant = montant;
        p.reference = reference;
        p.createdAt = now;
        return p;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public String getReference() {
        return reference;
    }
}

