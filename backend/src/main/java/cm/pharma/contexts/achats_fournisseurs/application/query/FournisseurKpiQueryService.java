package cm.pharma.contexts.achats_fournisseurs.application.query;

import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.BonCommandeJpaRepository;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.ReceptionFournisseurJpaRepository;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.RetourFournisseurJpaRepository;
import cm.pharma.contexts.referentiel.application.service.ParametresService;
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
    private final ParametresService parametres;

    public FournisseurKpiQueryService(
            BonCommandeJpaRepository bons,
            ReceptionFournisseurJpaRepository receptions,
            RetourFournisseurJpaRepository retours,
            ParametresService parametres
    ) {
        this.bons = Objects.requireNonNull(bons);
        this.receptions = Objects.requireNonNull(receptions);
        this.retours = Objects.requireNonNull(retours);
        this.parametres = Objects.requireNonNull(parametres);
    }

    public FournisseurKpis compute(UUID organisationId, UUID fournisseurId) {
        int fenetreJours = Math.max(1, parametres.getInt(organisationId, "FOURNISSEUR_KPI_FENETRE_JOURS", 180));
        Instant since = Instant.now().minus(fenetreJours, ChronoUnit.DAYS);

        List<cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.BonCommandeJpaEntity> bc = bons.findRecentByFournisseur(organisationId, fournisseurId, since);
        long bcComplets = bc.stream().filter(b -> "RECU_COMPLET".equals(b.getStatut())).count();
        long bcPartiels = bc.stream().filter(b -> "RECU_PARTIEL".equals(b.getStatut())).count();
        long bcReceptions = bcComplets + bcPartiels;
        double tauxLivraisonComplete = bcReceptions == 0 ? 1.0 : ((double) bcComplets / (double) bcReceptions);

        int nbReceptions = receptions.findRecentByFournisseur(organisationId, fournisseurId, since).size();
        int nbRetours = retours.findRecentByFournisseur(organisationId, fournisseurId, since).size();

        double poidsLivraisonPct = parametres.getDouble(organisationId, "FOURNISSEUR_KPI_POIDS_LIVRAISON_PCT", 80.0);
        double penaliteRetourParUnite = parametres.getDouble(organisationId, "FOURNISSEUR_KPI_PENALITE_RETOUR_PAR_UNITE", 2.0);
        double penaliteRetourMax = parametres.getDouble(organisationId, "FOURNISSEUR_KPI_PENALITE_RETOUR_MAX", 20.0);

        // V1: score simple (à raffiner quand on aura plus de signaux: délais, réclamations, etc.)
        int score = (int) Math.round((tauxLivraisonComplete * poidsLivraisonPct) - Math.min(penaliteRetourMax, nbRetours * penaliteRetourParUnite));
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

