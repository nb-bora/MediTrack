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
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
        BigDecimal priseTheorique = BigDecimal.ZERO;
        for (var l : lignesVente) {
            ProduitJpaEntity p = produits.findById(l.getProduitId()).orElseThrow(() -> new BusinessRuleViolationException("Produit introuvable"));
            BigDecimal taux = TiersPayantCalculator.tauxApplicable(c, p);
            total = total.add(l.getTotalLigne());
            priseTheorique = priseTheorique.add(TiersPayantCalculator.computePriseEnCharge(l.getTotalLigne(), taux));
        }
        BigDecimal prisePlafonnee = appliquerPlafonds(organisationId, vente.getOrganismeId(), vente.getPatientId(), c, priseTheorique);
        BigDecimal reste = total.subtract(prisePlafonnee);
        if (reste.compareTo(BigDecimal.ZERO) < 0) {
            reste = BigDecimal.ZERO;
        }
        if (prisePlafonnee.compareTo(BigDecimal.ZERO) < 0) {
            prisePlafonnee = BigDecimal.ZERO;
        }
        if (prisePlafonnee.compareTo(total) > 0) {
            prisePlafonnee = total;
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
                total.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : prisePlafonnee.multiply(new BigDecimal("100")).divide(total, 2, RoundingMode.HALF_UP),
                total,
                prisePlafonnee,
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

    private BigDecimal appliquerPlafonds(
            UUID organisationId,
            UUID organismeId,
            UUID patientId,
            OrganismeCouvertureJpaEntity c,
            BigDecimal priseTheorique
    ) {
        if (priseTheorique == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal remaining = priseTheorique;

        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now();
        Instant startDay = today.atStartOfDay(zone).toInstant();
        Instant startTomorrow = today.plusDays(1).atStartOfDay(zone).toInstant();
        Instant startMonth = today.withDayOfMonth(1).atStartOfDay(zone).toInstant();
        Instant startNextMonth = today.plusMonths(1).withDayOfMonth(1).atStartOfDay(zone).toInstant();
        Instant startYear = today.withDayOfYear(1).atStartOfDay(zone).toInstant();
        Instant startNextYear = today.plusYears(1).withDayOfYear(1).atStartOfDay(zone).toInstant();

        remaining = capByRemainingCeiling(remaining, c.getPlafondJournalier(),
                dossiers.sumPriseEnChargePeriode(organisationId, organismeId, patientId, startDay, startTomorrow));
        remaining = capByRemainingCeiling(remaining, c.getPlafondMensuel(),
                dossiers.sumPriseEnChargePeriode(organisationId, organismeId, patientId, startMonth, startNextMonth));
        remaining = capByRemainingCeiling(remaining, c.getPlafondAnnuel(),
                dossiers.sumPriseEnChargePeriode(organisationId, organismeId, patientId, startYear, startNextYear));

        return remaining;
    }

    private static BigDecimal capByRemainingCeiling(BigDecimal desired, BigDecimal plafond, BigDecimal dejaPris) {
        if (desired == null) {
            return BigDecimal.ZERO;
        }
        if (plafond == null) {
            return desired;
        }
        if (plafond.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (dejaPris == null) {
            dejaPris = BigDecimal.ZERO;
        }
        BigDecimal restant = plafond.subtract(dejaPris);
        if (restant.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return desired.min(restant);
    }
}

