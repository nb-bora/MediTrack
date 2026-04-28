package cm.pharma.contexts.assurance_mutuelle.application.command;

import cm.pharma.contexts.assurance_mutuelle.application.service.TiersPayantCalculator;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.DossierTiersPayantJpaEntity;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.DossierTiersPayantJpaRepository;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.OrganismeCouvertureJpaEntity;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.OrganismeCouvertureJpaRepository;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteJpaRepository;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteLigneJpaRepository;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaEntity;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaRepository;
import cm.pharma.contexts.referentiel.application.service.NumerotationService;
import cm.pharma.shared.application.AlerteService;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreerDossierTiersPayantDepuisVenteUseCase {

    private final VenteJpaRepository ventes;
    private final VenteLigneJpaRepository lignes;
    private final ProduitJpaRepository produits;
    private final OrganismeCouvertureJpaRepository couvertures;
    private final DossierTiersPayantJpaRepository dossiers;
    private final NumerotationService numerotation;
    private final AlerteService alertes;

    public CreerDossierTiersPayantDepuisVenteUseCase(
            VenteJpaRepository ventes,
            VenteLigneJpaRepository lignes,
            ProduitJpaRepository produits,
            OrganismeCouvertureJpaRepository couvertures,
            DossierTiersPayantJpaRepository dossiers,
            NumerotationService numerotation,
            AlerteService alertes
    ) {
        this.ventes = Objects.requireNonNull(ventes);
        this.lignes = Objects.requireNonNull(lignes);
        this.produits = Objects.requireNonNull(produits);
        this.couvertures = Objects.requireNonNull(couvertures);
        this.dossiers = Objects.requireNonNull(dossiers);
        this.numerotation = Objects.requireNonNull(numerotation);
        this.alertes = Objects.requireNonNull(alertes);
    }

    @Transactional
    public UUID execute(UUID organisationId, UUID venteId, UUID actorId) {
        VenteJpaEntity vente = ventes.findByOrganisationIdAndId(organisationId, venteId)
                .orElseThrow(() -> new BusinessRuleViolationException("Vente introuvable"));
        if (vente.getOrganismeId() == null || vente.getPatientId() == null) {
            throw new BusinessRuleViolationException("Contexte tiers payant manquant sur la vente (patient/organisme)");
        }

        OrganismeCouvertureJpaEntity c = couvertures.findByOrganisationIdAndOrganismeId(organisationId, vente.getOrganismeId())
                .orElseThrow(() -> new BusinessRuleViolationException("Couverture organisme non définie"));

        var lignesVente = lignes.findByVenteId(venteId);
        if (lignesVente.isEmpty()) {
            throw new BusinessRuleViolationException("Vente vide");
        }

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal prise = BigDecimal.ZERO;
        for (var l : lignesVente) {
            ProduitJpaEntity p = produits.findById(l.getProduitId()).orElseThrow(() -> new BusinessRuleViolationException("Produit introuvable"));
            BigDecimal taux = TiersPayantCalculator.tauxApplicable(c, p);
            total = total.add(l.getTotalLigne());
            prise = prise.add(TiersPayantCalculator.computePriseEnCharge(l.getTotalLigne(), taux));
        }
        BigDecimal reste = total.subtract(prise);
        if (reste.compareTo(BigDecimal.ZERO) < 0) {
            reste = BigDecimal.ZERO;
        }

        Instant now = Instant.now();
        UUID dossierId = UUID.randomUUID();
        String numero = numerotation.nextNumero(organisationId, "DOSSIER_TP");
        dossiers.save(DossierTiersPayantJpaEntity.create(new DossierTiersPayantJpaEntity.DossierInit(
                dossierId,
                organisationId,
                vente.getOrganismeId(),
                vente.getPatientId(),
                vente.getId(),
                vente.getOrdonnanceId(),
                numero,
                // Taux global non strict (on stocke la moyenne simple pour affichage)
                total.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : prise.multiply(new BigDecimal("100")).divide(total, 2, java.math.RoundingMode.HALF_UP),
                total,
                prise,
                reste,
                actorId,
                now
        )));

        // Si pièces obligatoires, on ouvrira une alerte “tâche” tant que le dossier est brouillon.
        if (c.isPieceOrdonnanceOriginale() || c.isPieceCarteAdherent() || c.isPieceBonPriseEnCharge() || c.isPieceExamens()) {
            alertes.openDedup(
                    organisationId,
                    "DOSSIER_TP_PIECES_A_VERIFIER",
                    "IMPORTANT",
                    "DossierTiersPayant",
                    dossierId.toString(),
                    "Vérifier/joindre les pièces requises avant soumission",
                    actorId
            );
        }

        return dossierId;
    }
}

