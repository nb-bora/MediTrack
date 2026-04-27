package cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "session_caisse")
public class SessionCaisseJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "poste_nom", nullable = false)
    private String posteNom;

    @Column(name = "statut", nullable = false, length = 20)
    private String statut;

    @Column(name = "caissier_id", nullable = false)
    private UUID caissierId;

    @Column(name = "fond_initial", nullable = false, precision = 19, scale = 4)
    private BigDecimal fondInitial;

    @Column(name = "devise", nullable = false, length = 3)
    private String devise;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "montant_reel_fermeture", precision = 19, scale = 4)
    private BigDecimal montantReelFermeture;

    @Column(name = "motif_ecart")
    private String motifEcart;

    protected SessionCaisseJpaEntity() {
    }

    public static SessionCaisseJpaEntity open(UUID id, UUID organisationId, String posteNom, UUID caissierId, BigDecimal fondInitial, String devise, Instant now) {
        SessionCaisseJpaEntity s = new SessionCaisseJpaEntity();
        s.id = id;
        s.organisationId = organisationId;
        s.posteNom = posteNom;
        s.statut = "OUVERTE";
        s.caissierId = caissierId;
        s.fondInitial = fondInitial;
        s.devise = devise;
        s.openedAt = now;
        return s;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public String getPosteNom() {
        return posteNom;
    }

    public String getStatut() {
        return statut;
    }

    public UUID getCaissierId() {
        return caissierId;
    }

    public void close(BigDecimal montantReel, String motifEcart, Instant now) {
        this.statut = "FERMEE";
        this.closedAt = now;
        this.montantReelFermeture = montantReel;
        this.motifEcart = motifEcart;
    }
}

