package cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "retour_vente_ligne")
public class RetourVenteLigneJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "retour_vente_id", nullable = false)
    private UUID retourVenteId;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "vente_ligne_id", nullable = false)
    private UUID venteLigneId;

    @Column(name = "lot_id", nullable = false)
    private UUID lotId;

    @Column(name = "emplacement_id", nullable = false)
    private UUID emplacementId;

    @Column(name = "quantite", nullable = false)
    private int quantite;

    @Column(name = "montant_ligne", nullable = false, precision = 19, scale = 4)
    private BigDecimal montantLigne;

    protected RetourVenteLigneJpaEntity() {
    }

    public static RetourVenteLigneJpaEntity create(UUID id, UUID retourVenteId, UUID organisationId, UUID venteLigneId,
                                                   UUID lotId, UUID emplacementId, int quantite, BigDecimal montantLigne) {
        RetourVenteLigneJpaEntity l = new RetourVenteLigneJpaEntity();
        l.id = id;
        l.retourVenteId = retourVenteId;
        l.organisationId = organisationId;
        l.venteLigneId = venteLigneId;
        l.lotId = lotId;
        l.emplacementId = emplacementId;
        l.quantite = quantite;
        l.montantLigne = montantLigne == null ? BigDecimal.ZERO : montantLigne;
        return l;
    }
}

