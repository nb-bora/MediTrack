package cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "organisme_couverture")
public class OrganismeCouvertureJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisme_id", nullable = false)
    private UUID organismeId;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "taux_generique", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxGenerique;

    @Column(name = "taux_marque", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxMarque;

    @Column(name = "taux_parapharma", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxParapharma;

    @Column(name = "taux_stupefiants", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxStupefiants;

    @Column(name = "plafond_journalier", precision = 19, scale = 4)
    private BigDecimal plafondJournalier;

    @Column(name = "plafond_mensuel", precision = 19, scale = 4)
    private BigDecimal plafondMensuel;

    @Column(name = "plafond_annuel", precision = 19, scale = 4)
    private BigDecimal plafondAnnuel;

    @Column(name = "piece_ordonnance_originale", nullable = false)
    private boolean pieceOrdonnanceOriginale;

    @Column(name = "piece_carte_adherent", nullable = false)
    private boolean pieceCarteAdherent;

    @Column(name = "piece_bon_prise_en_charge", nullable = false)
    private boolean pieceBonPriseEnCharge;

    @Column(name = "piece_examens", nullable = false)
    private boolean pieceExamens;

    protected OrganismeCouvertureJpaEntity() {
    }

    public record CouvertureInit(
            UUID id,
            UUID organismeId,
            UUID organisationId,
            BigDecimal tauxGenerique,
            BigDecimal tauxMarque,
            BigDecimal tauxParapharma,
            BigDecimal tauxStupefiants,
            BigDecimal plafondJournalier,
            BigDecimal plafondMensuel,
            BigDecimal plafondAnnuel,
            boolean pieceOrdonnanceOriginale,
            boolean pieceCarteAdherent,
            boolean pieceBonPriseEnCharge,
            boolean pieceExamens
    ) {
    }

    public static OrganismeCouvertureJpaEntity createOrUpdate(OrganismeCouvertureJpaEntity existing, CouvertureInit init) {
        OrganismeCouvertureJpaEntity c = existing == null ? new OrganismeCouvertureJpaEntity() : existing;
        if (existing == null) {
            c.id = init.id();
            c.organismeId = init.organismeId();
            c.organisationId = init.organisationId();
        }
        c.tauxGenerique = init.tauxGenerique();
        c.tauxMarque = init.tauxMarque();
        c.tauxParapharma = init.tauxParapharma();
        c.tauxStupefiants = init.tauxStupefiants();
        c.plafondJournalier = init.plafondJournalier();
        c.plafondMensuel = init.plafondMensuel();
        c.plafondAnnuel = init.plafondAnnuel();
        c.pieceOrdonnanceOriginale = init.pieceOrdonnanceOriginale();
        c.pieceCarteAdherent = init.pieceCarteAdherent();
        c.pieceBonPriseEnCharge = init.pieceBonPriseEnCharge();
        c.pieceExamens = init.pieceExamens();
        return c;
    }

    public UUID getOrganismeId() {
        return organismeId;
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getTauxGenerique() {
        return tauxGenerique;
    }

    public BigDecimal getTauxMarque() {
        return tauxMarque;
    }

    public BigDecimal getTauxParapharma() {
        return tauxParapharma;
    }

    public BigDecimal getTauxStupefiants() {
        return tauxStupefiants;
    }

    public BigDecimal getPlafondJournalier() {
        return plafondJournalier;
    }

    public BigDecimal getPlafondMensuel() {
        return plafondMensuel;
    }

    public BigDecimal getPlafondAnnuel() {
        return plafondAnnuel;
    }

    public boolean isPieceOrdonnanceOriginale() {
        return pieceOrdonnanceOriginale;
    }

    public boolean isPieceCarteAdherent() {
        return pieceCarteAdherent;
    }

    public boolean isPieceBonPriseEnCharge() {
        return pieceBonPriseEnCharge;
    }

    public boolean isPieceExamens() {
        return pieceExamens;
    }
}

