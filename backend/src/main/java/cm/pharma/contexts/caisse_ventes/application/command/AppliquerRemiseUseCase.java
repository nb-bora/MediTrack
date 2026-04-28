package cm.pharma.contexts.caisse_ventes.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteJpaRepository;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteLigneJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteLigneJpaRepository;
import cm.pharma.contexts.referentiel.application.service.ParametresService;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppliquerRemiseUseCase {

    private final VenteJpaRepository ventes;
    private final VenteLigneJpaRepository lignes;
    private final ParametresService parametres;
    private final AuditWriter auditWriter;

    public AppliquerRemiseUseCase(VenteJpaRepository ventes, VenteLigneJpaRepository lignes, ParametresService parametres, AuditWriter auditWriter) {
        this.ventes = Objects.requireNonNull(ventes);
        this.lignes = Objects.requireNonNull(lignes);
        this.parametres = Objects.requireNonNull(parametres);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public void execute(UUID organisationId, UUID venteId, UUID venteLigneId, BigDecimal remisePct, String motif, UUID actorId, String posteNom, boolean isCaissier, boolean isPharmacien, boolean isAdmin) {
        if (remisePct == null || remisePct.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("Remise invalide");
        }
        if (remisePct.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessRuleViolationException("Remise invalide (>100%)");
        }

        // Règles rôles
        BigDecimal maxCaissier = parametres.getBigDecimal(organisationId, "REMISE_MAX_CAISSIER_PCT", new BigDecimal("5"));
        BigDecimal maxPharmacien = parametres.getBigDecimal(organisationId, "REMISE_MAX_PHARMACIEN_PCT", new BigDecimal("15"));

        if (isCaissier && remisePct.compareTo(maxCaissier) > 0) {
            throw new BusinessRuleViolationException("Remise caissier limitée à " + maxCaissier + "%");
        }
        if (isPharmacien && remisePct.compareTo(maxPharmacien) > 0 && !isAdmin) {
            throw new BusinessRuleViolationException("Remise pharmacien limitée à " + maxPharmacien + "% (au-delà: admin)");
        }
        if (!isAdmin && remisePct.compareTo(maxPharmacien) > 0 && !isPharmacien) {
            throw new BusinessRuleViolationException("Remise exceptionnelle réservée à l'admin");
        }
        if (remisePct.compareTo(maxPharmacien) > 0 && (motif == null || motif.isBlank())) {
            throw new BusinessRuleViolationException("Motif obligatoire pour remise exceptionnelle");
        }

        VenteJpaEntity vente = ventes.findByOrganisationIdAndId(organisationId, venteId)
                .orElseThrow(() -> new BusinessRuleViolationException("Vente introuvable"));
        if (!"BROUILLON".equals(vente.getStatut())) {
            throw new BusinessRuleViolationException("Remise interdite sur ce statut");
        }

        VenteLigneJpaEntity ligne = lignes.findById(venteLigneId)
                .orElseThrow(() -> new BusinessRuleViolationException("Ligne introuvable"));
        if (!ligne.getVenteId().equals(venteId)) {
            throw new BusinessRuleViolationException("Ligne hors vente");
        }

        BigDecimal base = ligne.getPrixUnitaireTtc().multiply(BigDecimal.valueOf(ligne.getQuantite()));
        BigDecimal remise = base.multiply(remisePct).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        ligne.appliquerRemise(remise);

        // Recalcul totaux vente (sans toucher à l’arrondi)
        BigDecimal totalTtc = lignes.findByVenteId(venteId).stream()
                .map(VenteLigneJpaEntity::getTotalLigne)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRemise = lignes.findByVenteId(venteId).stream()
                .map(VenteLigneJpaEntity::getRemise)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vente.setTotals(totalTtc, totalRemise);

        Instant now = Instant.now();
        auditWriter.write(AuditEvent.simple(
                organisationId, now, actorId, null, null,
                posteNom, null, "VENTE_REMISE_APPLIQUEE", "Vente", vente.getNumeroVente(), motif,
                Map.of("vente_ligne_id", venteLigneId, "remise_pct", remisePct, "remise_montant", remise)
        ));
    }
}

