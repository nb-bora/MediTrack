package cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "prix_produit")
public class PrixProduitJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "produit_id", nullable = false)
    private UUID produitId;

    @Column(name = "type_prix", nullable = false, length = 10)
    private String typePrix;

    @Column(name = "montant", nullable = false, precision = 19, scale = 4)
    private BigDecimal montant;

    @Column(name = "devise", nullable = false, length = 3)
    private String devise;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "motif")
    private String motif;

    @Column(name = "cree_par")
    private UUID creePar;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PrixProduitJpaEntity() {
    }

    public record PrixInit(
            UUID id,
            UUID produitId,
            String typePrix,
            BigDecimal montant,
            String devise,
            LocalDate dateDebut,
            LocalDate dateFin,
            String motif,
            UUID creePar,
            Instant now
    ) {
    }

    public static PrixProduitJpaEntity create(PrixInit init) {
        PrixProduitJpaEntity p = new PrixProduitJpaEntity();
        p.id = init.id();
        p.produitId = init.produitId();
        p.typePrix = init.typePrix();
        p.montant = init.montant();
        p.devise = init.devise();
        p.dateDebut = init.dateDebut();
        p.dateFin = init.dateFin();
        p.motif = init.motif();
        p.creePar = init.creePar();
        p.createdAt = init.now();
        return p;
    }

    public UUID getId() {
        return id;
    }

    public java.math.BigDecimal getMontant() {
        return montant;
    }

    public String getDevise() {
        return devise;
    }

    public UUID getProduitId() {
        return produitId;
    }

    public String getTypePrix() {
        return typePrix;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void closeAt(LocalDate endDate) {
        this.dateFin = endDate;
    }
}

