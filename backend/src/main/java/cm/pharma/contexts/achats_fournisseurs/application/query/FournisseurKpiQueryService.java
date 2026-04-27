package cm.pharma.contexts.achats_fournisseurs.application.query;

import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.BonCommandeJpaRepository;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.ReceptionFournisseurJpaRepository;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.RetourFournisseurJpaRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class FournisseurKpiQueryService {

    private final BonCommandeJpaRepository bons;
    private final ReceptionFournisseurJpaRepository receptions;
    private final RetourFournisseurJpaRepository retours;

    public FournisseurKpiQueryService(
            BonCommandeJpaRepository bons,
            ReceptionFournisseurJpaRepository receptions,
            RetourFournisseurJpaRepository retours
    ) {
        this.bons = Objects.requireNonNull(bons);
        this.receptions = Objects.requireNonNull(receptions);
        this.retours = Objects.requireNonNull(retours);
    }

    public FournisseurKpis compute(UUID organisationId, UUID fournisseurId) {
        Instant since = Instant.now().minus(180, ChronoUnit.DAYS);

        List<cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.BonCommandeJpaEntity> bc = bons.findRecentByFournisseur(organisationId, fournisseurId, since);
        long bcComplets = bc.stream().filter(b -> "RECU_COMPLET".equals(b.getStatut())).count();
        long bcPartiels = bc.stream().filter(b -> "RECU_PARTIEL".equals(b.getStatut())).count();
        long bcReceptions = bcComplets + bcPartiels;
        double tauxLivraisonComplete = bcReceptions == 0 ? 1.0 : ((double) bcComplets / (double) bcReceptions);

        int nbReceptions = receptions.findRecentByFournisseur(organisationId, fournisseurId, since).size();
        int nbRetours = retours.findRecentByFournisseur(organisationId, fournisseurId, since).size();

        // V1: score simple (à raffiner quand on aura plus de signaux: délais, réclamations, etc.)
        int score = (int) Math.round((tauxLivraisonComplete * 80.0) - Math.min(20.0, nbRetours * 2.0));
        if (score < 0) {
            score = 0;
        }
        if (score > 100) {
            score = 100;
        }
        return new FournisseurKpis(tauxLivraisonComplete, nbReceptions, nbRetours, score);
    }

    public record FournisseurKpis(double tauxLivraisonComplete, int nbReceptions, int nbRetours, int scoreSur100) {
    }
}

