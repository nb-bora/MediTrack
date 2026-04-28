package cm.pharma.contexts.ordonnances_dossier_patient.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaEntity;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaRepository;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.DispensationJpaEntity;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.DispensationJpaRepository;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceJpaEntity;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceJpaRepository;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceLigneJpaEntity;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceLigneJpaRepository;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.PatientMedicalJpaRepository;
import cm.pharma.contexts.ordonnances_dossier_patient.application.service.AllergieDetector;
import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.EmplacementJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.MouvementStockJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.MouvementStockJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.StockEmplacementJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.StockEmplacementJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.InventaireJpaRepository;
import cm.pharma.shared.application.AlerteService;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DispenserOrdonnanceUseCase {

    private static final String EMPLACEMENT_COMPTOIR_MED = "COMPTOIR_MED";

    private final OrdonnanceJpaRepository ordonnances;
    private final OrdonnanceLigneJpaRepository lignes;
    private final DispensationJpaRepository dispensations;
    private final ProduitJpaRepository produits;
    private final PatientMedicalJpaRepository patientMedical;
    private final EmplacementJpaRepository emplacements;
    private final StockEmplacementJpaRepository stock;
    private final MouvementStockJpaRepository mouvements;
    private final InventaireJpaRepository inventaires;
    private final AlerteService alerteService;
    private final AuditWriter auditWriter;

    public DispenserOrdonnanceUseCase(
            OrdonnanceJpaRepository ordonnances,
            OrdonnanceLigneJpaRepository lignes,
            DispensationJpaRepository dispensations,
            ProduitJpaRepository produits,
            PatientMedicalJpaRepository patientMedical,
            EmplacementJpaRepository emplacements,
            StockEmplacementJpaRepository stock,
            MouvementStockJpaRepository mouvements,
            InventaireJpaRepository inventaires,
            AlerteService alerteService,
            AuditWriter auditWriter
    ) {
        this.ordonnances = Objects.requireNonNull(ordonnances);
        this.lignes = Objects.requireNonNull(lignes);
        this.dispensations = Objects.requireNonNull(dispensations);
        this.produits = Objects.requireNonNull(produits);
        this.patientMedical = Objects.requireNonNull(patientMedical);
        this.emplacements = Objects.requireNonNull(emplacements);
        this.stock = Objects.requireNonNull(stock);
        this.mouvements = Objects.requireNonNull(mouvements);
        this.inventaires = Objects.requireNonNull(inventaires);
        this.alerteService = Objects.requireNonNull(alerteService);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public void execute(DispenserCommand cmd) {
        Objects.requireNonNull(cmd);
        inventaires.findOuvert(cmd.organisationId()).ifPresent(i -> {
            throw new BusinessRuleViolationException("Mouvements bloqués: inventaire ouvert");
        });

        if (cmd.quantite() <= 0) {
            throw new BusinessRuleViolationException("Quantité invalide");
        }

        OrdonnanceJpaEntity o = ordonnances.findByOrganisationIdAndId(cmd.organisationId(), cmd.ordonnanceId())
                .orElseThrow(() -> new BusinessRuleViolationException("Ordonnance introuvable"));
        if (!List.of("VALIDEE", "PARTIELLEMENT_DISPENSEE").contains(o.getStatut())) {
            throw new BusinessRuleViolationException("Ordonnance non dispensable (statut)");
        }
        if (o.getDateExpiration().isBefore(LocalDate.now())) {
            throw new BusinessRuleViolationException("Ordonnance expirée");
        }

        OrdonnanceLigneJpaEntity ligne = lignes.findByOrganisationIdAndId(cmd.organisationId(), cmd.ordonnanceLigneId())
                .orElseThrow(() -> new BusinessRuleViolationException("Ligne ordonnance introuvable"));
        if (!ligne.getOrdonnanceId().equals(cmd.ordonnanceId())) {
            throw new BusinessRuleViolationException("Ligne hors ordonnance");
        }
        int restant = ligne.getQuantitePrescrite() - ligne.getQuantiteDispensee();
        if (cmd.quantite() > restant) {
            throw new BusinessRuleViolationException("Quantité > restant à dispenser");
        }

        ProduitJpaEntity produit = produits.findById(ligne.getProduitId())
                .orElseThrow(() -> new BusinessRuleViolationException("Produit introuvable"));
        if (produit.isNecessiteOrdonnance() && !cmd.peutOverriderAllergie()) {
            // l’ordonnance est déjà validée par pharmacien; ici on garde le contrôle allergie/override.
        }

        boolean risque = verifierAllergieEtBloquerSiNecessaire(cmd, o, produit);

        // Déstockage FEFO au comptoir médicaments (traçabilité lot).
        UUID comptoirId = emplacements.findByOrganisationIdAndCode(cmd.organisationId(), EMPLACEMENT_COMPTOIR_MED)
                .orElseThrow(() -> new BusinessRuleViolationException("Emplacement comptoir médicaments introuvable"))
                .getId();

        List<StockEmplacementJpaRepository.StockLotDisponibleRow> lotsDisponibles = stock.findLotsActifsDisponibles(cmd.organisationId(), comptoirId, ligne.getProduitId()).stream()
                .sorted(Comparator.comparing(StockEmplacementJpaRepository.StockLotDisponibleRow::getDatePeremption)
                        .thenComparing(StockEmplacementJpaRepository.StockLotDisponibleRow::getNumeroLot))
                .toList();

        Instant now = Instant.now();
        consommerLotsFefoEtTracer(cmd, ligne, lotsDisponibles, comptoirId, risque, now);

        ligne.incrementerDispensee(cmd.quantite());

        // Maj statut ordonnance (partielle/complète)
        List<OrdonnanceLigneJpaEntity> all = lignes.findByOrdonnanceId(cmd.ordonnanceId());
        int totalPrescrit = all.stream().mapToInt(OrdonnanceLigneJpaEntity::getQuantitePrescrite).sum();
        int totalDispense = all.stream().mapToInt(OrdonnanceLigneJpaEntity::getQuantiteDispensee).sum();
        o.majStatutDispensation(totalPrescrit, totalDispense);

        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, cmd.actorId(), null, null,
                cmd.posteNom(), null, "DISPENSATION_EFFECTUEE", "Ordonnance", cmd.ordonnanceId().toString(), cmd.motifOverride(),
                Map.of("ordonnance_ligne_id", ligne.getId(), "produit_id", ligne.getProduitId(), "quantite", cmd.quantite())
        ));
    }

    private boolean verifierAllergieEtBloquerSiNecessaire(DispenserCommand cmd, OrdonnanceJpaEntity o, ProduitJpaEntity produit) {
        String allergies = patientMedical.findByPatientId(o.getPatientId()).map(pm -> pm.getAllergies()).orElse(null);
        boolean risque = AllergieDetector.isBlocant(allergies, produit);
        if (!risque) {
            return false;
        }
        alerteService.openDedup(cmd.organisationId(), "ALLERGIE_MEDICAMENTEUSE", "URGENT", "Patient", o.getPatientId().toString(),
                "Alerte allergie: vérifier dispensation (" + produit.getNomCommercial() + ")", cmd.actorId());
        if (cmd.motifOverride() == null || cmd.motifOverride().isBlank()) {
            throw new BusinessRuleViolationException("Dispensation bloquée (allergie) — override pharmacien avec motif requis");
        }
        if (!cmd.peutOverriderAllergie()) {
            throw new BusinessRuleViolationException("Override allergie réservé au pharmacien/admin");
        }
        return true;
    }

    @Transactional(readOnly = true)
    public DispensationPreview preview(PreviewCommand cmd) {
        Objects.requireNonNull(cmd);
        if (cmd.quantiteSouhaitee() <= 0) {
            throw new BusinessRuleViolationException("Quantité invalide");
        }
        OrdonnanceJpaEntity o = ordonnances.findByOrganisationIdAndId(cmd.organisationId(), cmd.ordonnanceId())
                .orElseThrow(() -> new BusinessRuleViolationException("Ordonnance introuvable"));
        OrdonnanceLigneJpaEntity ligne = lignes.findByOrganisationIdAndId(cmd.organisationId(), cmd.ordonnanceLigneId())
                .orElseThrow(() -> new BusinessRuleViolationException("Ligne ordonnance introuvable"));
        if (!ligne.getOrdonnanceId().equals(cmd.ordonnanceId())) {
            throw new BusinessRuleViolationException("Ligne hors ordonnance");
        }
        int restant = ligne.getQuantitePrescrite() - ligne.getQuantiteDispensee();
        int cible = Math.min(cmd.quantiteSouhaitee(), restant);
        if (cible <= 0) {
            return new DispensationPreview(0, 0, List.of());
        }

        UUID comptoirId = emplacements.findByOrganisationIdAndCode(cmd.organisationId(), EMPLACEMENT_COMPTOIR_MED)
                .orElseThrow(() -> new BusinessRuleViolationException("Emplacement comptoir médicaments introuvable"))
                .getId();

        List<StockEmplacementJpaRepository.StockLotDisponibleRow> lotsDisponibles = stock.findLotsActifsDisponibles(cmd.organisationId(), comptoirId, ligne.getProduitId()).stream()
                .sorted(java.util.Comparator.comparing(StockEmplacementJpaRepository.StockLotDisponibleRow::getDatePeremption)
                        .thenComparing(StockEmplacementJpaRepository.StockLotDisponibleRow::getNumeroLot))
                .toList();

        int available = lotsDisponibles.stream().mapToInt(StockEmplacementJpaRepository.StockLotDisponibleRow::getQuantite).sum();
        int propose = Math.min(cible, available);

        List<AllocationLot> allocations = buildAllocations(lotsDisponibles, propose);
        return new DispensationPreview(cible, propose, allocations);
    }

    private static List<AllocationLot> buildAllocations(List<StockEmplacementJpaRepository.StockLotDisponibleRow> lotsDisponibles, int quantite) {
        if (quantite <= 0) {
            return List.of();
        }
        int remaining = quantite;
        java.util.ArrayList<AllocationLot> out = new java.util.ArrayList<>();
        for (var row : lotsDisponibles) {
            if (remaining <= 0) break;
            int take = Math.min(row.getQuantite(), remaining);
            out.add(new AllocationLot(row.getLotId(), take, row.getDatePeremption(), row.getNumeroLot()));
            remaining -= take;
        }
        return out;
    }

    private void consommerLotsFefoEtTracer(
            DispenserCommand cmd,
            OrdonnanceLigneJpaEntity ligne,
            List<StockEmplacementJpaRepository.StockLotDisponibleRow> lotsDisponibles,
            UUID comptoirId,
            boolean risque,
            Instant now
    ) {
        int remaining = cmd.quantite();
        for (var row : lotsDisponibles) {
            if (remaining <= 0) {
                break;
            }
            int take = Math.min(row.getQuantite(), remaining);
            consommerLot(cmd, ligne, comptoirId, row.getLotId(), take, risque, now);
            remaining -= take;
        }
        if (remaining > 0) {
            throw new BusinessRuleViolationException("Stock insuffisant au comptoir pour dispensation");
        }
    }

    private void consommerLot(
            DispenserCommand cmd,
            OrdonnanceLigneJpaEntity ligne,
            UUID comptoirId,
            UUID lotId,
            int quantite,
            boolean risque,
            Instant now
    ) {
        StockEmplacementJpaEntity se = stock.findByOrganisationIdAndEmplacementIdAndLotId(cmd.organisationId(), comptoirId, lotId)
                .orElseThrow(() -> new BusinessRuleViolationException("Stock introuvable (concurrence)"));
        int newQty = se.getQuantite() - quantite;
        if (newQty < 0) {
            throw new BusinessRuleViolationException("Stock négatif détecté — recommencer");
        }
        se.setQuantite(newQty, now);
        stock.save(se);

        dispensations.save(DispensationJpaEntity.create(
                UUID.randomUUID(),
                cmd.organisationId(),
                cmd.ordonnanceId(),
                ligne.getId(),
                ligne.getProduitId(),
                quantite,
                lotId,
                comptoirId,
                cmd.actorId(),
                risque ? cmd.motifOverride() : null,
                now
        ));

        mouvements.save(MouvementStockJpaEntity.create(new MouvementStockJpaEntity.MouvementInit(
                UUID.randomUUID(),
                cmd.organisationId(),
                "DISPENSATION",
                lotId,
                ligne.getProduitId(),
                quantite,
                comptoirId,
                null,
                cmd.ordonnanceId().toString(),
                risque ? cmd.motifOverride() : null,
                cmd.actorId(),
                now
        )));
    }

    public record DispenserCommand(
            UUID organisationId,
            UUID ordonnanceId,
            UUID ordonnanceLigneId,
            int quantite,
            UUID actorId,
            String posteNom,
            boolean peutOverriderAllergie,
            String motifOverride
    ) {
    }

    public record PreviewCommand(UUID organisationId, UUID ordonnanceId, UUID ordonnanceLigneId, int quantiteSouhaitee) {
    }

    public record DispensationPreview(int quantiteDemandee, int quantiteProposee, List<AllocationLot> allocations) {
    }

    public record AllocationLot(UUID lotId, int quantite, java.time.LocalDate datePeremption, String numeroLot) {
    }
}

