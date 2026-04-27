package cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "retour_fournisseur_ligne")
public class RetourFournisseurLigneJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "retour_id", nullable = false)
    private UUID retourId;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "lot_id", nullable = false)
    private UUID lotId;

    @Column(name = "quantite", nullable = false)
    private int quantite;

    @Column(name = "motif")
    private String motif;

    protected RetourFournisseurLigneJpaEntity() {
    }

    public static RetourFournisseurLigneJpaEntity create(UUID id, UUID retourId, UUID organisationId, UUID lotId, int quantite, String motif) {
        RetourFournisseurLigneJpaEntity l = new RetourFournisseurLigneJpaEntity();
        l.id = id;
        l.retourId = retourId;
        l.organisationId = organisationId;
        l.lotId = lotId;
        l.quantite = quantite;
        l.motif = motif;
        return l;
    }

    public UUID getLotId() {
        return lotId;
    }

    public int getQuantite() {
        return quantite;
    }
}

