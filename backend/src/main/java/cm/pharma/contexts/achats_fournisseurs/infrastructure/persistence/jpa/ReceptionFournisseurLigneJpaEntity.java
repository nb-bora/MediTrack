package cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "reception_fournisseur_ligne")
public class ReceptionFournisseurLigneJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "reception_id", nullable = false)
    private UUID receptionId;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "produit_id", nullable = false)
    private UUID produitId;

    @Column(name = "emplacement_destination_id", nullable = false)
    private UUID emplacementDestinationId;

    @Column(name = "numero_lot", nullable = false)
    private String numeroLot;

    @Column(name = "date_peremption", nullable = false)
    private LocalDate datePeremption;

    @Column(name = "quantite_recue", nullable = false)
    private int quantiteRecue;

    @Column(name = "prix_facture_unitaire", precision = 19, scale = 4)
    private BigDecimal prixFactureUnitaire;

    @Column(name = "devise", nullable = false, length = 3)
    private String devise;

    @Column(name = "temperature_transport_c", precision = 6, scale = 2)
    private BigDecimal temperatureTransportC;

    protected ReceptionFournisseurLigneJpaEntity() {
    }

    public static ReceptionFournisseurLigneJpaEntity create(
            UUID id,
            UUID receptionId,
            UUID organisationId,
            UUID produitId,
            UUID emplacementDestinationId,
            String numeroLot,
            LocalDate datePeremption,
            int quantiteRecue,
            BigDecimal prixFactureUnitaire,
            String devise,
            BigDecimal temperatureTransportC
    ) {
        ReceptionFournisseurLigneJpaEntity l = new ReceptionFournisseurLigneJpaEntity();
        l.id = id;
        l.receptionId = receptionId;
        l.organisationId = organisationId;
        l.produitId = produitId;
        l.emplacementDestinationId = emplacementDestinationId;
        l.numeroLot = numeroLot;
        l.datePeremption = datePeremption;
        l.quantiteRecue = quantiteRecue;
        l.prixFactureUnitaire = prixFactureUnitaire;
        l.devise = devise;
        l.temperatureTransportC = temperatureTransportC;
        return l;
    }
}

