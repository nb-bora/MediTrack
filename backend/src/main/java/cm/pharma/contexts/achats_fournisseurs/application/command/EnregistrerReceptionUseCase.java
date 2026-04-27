package cm.pharma.contexts.achats_fournisseurs.application.command;

import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.BonCommandeJpaEntity;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.BonCommandeJpaRepository;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.BonCommandeLigneJpaEntity;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.BonCommandeLigneJpaRepository;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.ReceptionFournisseurJpaEntity;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.ReceptionFournisseurJpaRepository;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.ReceptionFournisseurLigneJpaEntity;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.ReceptionFournisseurLigneJpaRepository;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.stocks_tracabilite.application.command.ReceptionnerStockCommand;
import cm.pharma.contexts.stocks_tracabilite.application.command.ReceptionnerStockUseCase;
import cm.pharma.shared.application.AlerteService;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnregistrerReceptionUseCase {
    private static final String ALERT_TYPE_PRIX_DIFFERENT = "PRIX_RECEPTION_DIFFERENT";

    private final BonCommandeJpaRepository bons;
    private final BonCommandeLigneJpaRepository lignesBon;
    private final ReceptionFournisseurJpaRepository receptions;
    private final ReceptionFournisseurLigneJpaRepository lignesReception;
    private final ReceptionnerStockUseCase receptionnerStock;
    private final AuditWriter auditWriter;
    private final AlerteService alerteService;

    public EnregistrerReceptionUseCase(
            BonCommandeJpaRepository bons,
            BonCommandeLigneJpaRepository lignesBon,
            ReceptionFournisseurJpaRepository receptions,
            ReceptionFournisseurLigneJpaRepository lignesReception,
            ReceptionnerStockUseCase receptionnerStock,
            AuditWriter auditWriter,
            AlerteService alerteService
    ) {
        this.bons = Objects.requireNonNull(bons);
        this.lignesBon = Objects.requireNonNull(lignesBon);
        this.receptions = Objects.requireNonNull(receptions);
        this.lignesReception = Objects.requireNonNull(lignesReception);
        this.receptionnerStock = Objects.requireNonNull(receptionnerStock);
        this.auditWriter = Objects.requireNonNull(auditWriter);
        this.alerteService = Objects.requireNonNull(alerteService);
    }

    @Transactional
    public UUID execute(EnregistrerReceptionCommand cmd) {
        Objects.requireNonNull(cmd);
        if (cmd.lignes() == null || cmd.lignes().isEmpty()) {
            throw new BusinessRuleViolationException("Réception sans lignes");
        }

        BonCommandeReceptionContext ctx = chargerContexteBonCommande(cmd);

        Instant now = Instant.now();
        UUID receptionId = UUID.randomUUID();
        ReceptionFournisseurJpaEntity reception = ReceptionFournisseurJpaEntity.create(
                receptionId,
                cmd.organisationId(),
                cmd.bonCommandeId(),
                cmd.fournisseurId(),
                cmd.referenceDocument(),
                cmd.creePar(),
                now
        );
        receptions.save(reception);

        boolean prixMismatch = false;
        for (EnregistrerReceptionCommand.LigneReception lr : cmd.lignes()) {
            BonCommandeLigneJpaEntity ligneBc = ctx.lignesByProduit.get(lr.produitId());
            Integer qteCommandee = (ligneBc == null) ? null : ligneBc.getQuantiteCommandee();

            receptionnerStock.execute(new ReceptionnerStockCommand(
                    cmd.organisationId(),
                    lr.produitId(),
                    lr.emplacementDestinationId(),
                    lr.numeroLot(),
                    lr.datePeremption(),
                    lr.quantiteRecue(),
                    null,
                    qteCommandee,
                    null,
                    lr.prixFactureUnitaire(),
                    lr.temperatureTransportC(),
                    lr.confirmerPeremptionProche(),
                    cmd.referenceDocument(),
                    null,
                    cmd.creePar()
            ));

            lignesReception.save(ReceptionFournisseurLigneJpaEntity.create(
                    UUID.randomUUID(),
                    receptionId,
                    cmd.organisationId(),
                    lr.produitId(),
                    lr.emplacementDestinationId(),
                    lr.numeroLot(),
                    lr.datePeremption(),
                    lr.quantiteRecue(),
                    lr.prixFactureUnitaire(),
                    (lr.devise() == null || lr.devise().isBlank()) ? "XAF" : lr.devise(),
                    lr.temperatureTransportC() == null ? null : BigDecimal.valueOf(lr.temperatureTransportC())
            ));

            if (ligneBc != null) {
                ligneBc.incrementerQuantiteRecue(lr.quantiteRecue());
            }

            // Workflow "prix différent" : si on réceptionne avec prix facture renseigné et qu’un prix attendu existe côté BC,
            // on ouvre une alerte et on marque la réception "EN_ATTENTE".
            // V1 : prix attendu n’est pas exposé par l’entité (getter absent), donc on déclenche seulement si le client envoie un prix facture.
            prixMismatch = prixMismatch || shouldFlagPrixValidation(cmd, lr);
        }

        if (prixMismatch) {
            reception.setStatutValidationPrix("EN_ATTENTE");
            alerteService.openDedup(
                    cmd.organisationId(),
                    ALERT_TYPE_PRIX_DIFFERENT,
                    "IMPORTANT",
                    "ReceptionFournisseur",
                    receptionId.toString(),
                    "Prix facture à valider (réception " + receptionId + ")",
                    cmd.creePar()
            );
        }

        if (ctx.bonCommande != null) {
            majStatutBonCommandeApresReception(ctx.bonCommande);
        }

        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, null, null, null,
                null, null, "RECEPTION_FOURNISSEUR_ENREGISTREE", "ReceptionFournisseur", receptionId.toString(), null,
                Map.of("bon_commande_id", cmd.bonCommandeId(), "fournisseur_id", cmd.fournisseurId())
        ));

        return receptionId;
    }

    private static boolean shouldFlagPrixValidation(EnregistrerReceptionCommand cmd, EnregistrerReceptionCommand.LigneReception lr) {
        return cmd.bonCommandeId() != null && lr.prixFactureUnitaire() != null;
    }

    private record BonCommandeReceptionContext(BonCommandeJpaEntity bonCommande, Map<UUID, BonCommandeLigneJpaEntity> lignesByProduit) {
    }

    private BonCommandeReceptionContext chargerContexteBonCommande(EnregistrerReceptionCommand cmd) {
        if (cmd.bonCommandeId() == null) {
            return new BonCommandeReceptionContext(null, Map.of());
        }
        BonCommandeJpaEntity bc = bons.findByOrganisationIdAndId(cmd.organisationId(), cmd.bonCommandeId())
                .orElseThrow(() -> new BusinessRuleViolationException("Bon de commande introuvable"));
        if (!bc.getFournisseurId().equals(cmd.fournisseurId())) {
            throw new BusinessRuleViolationException("Fournisseur différent de celui du bon de commande");
        }
        if (!java.util.Set.of("VALIDE", "RECU_PARTIEL").contains(bc.getStatut())) {
            throw new BusinessRuleViolationException("Bon de commande non réceptionnable");
        }
        Map<UUID, BonCommandeLigneJpaEntity> lignesByProduit = new HashMap<>();
        for (BonCommandeLigneJpaEntity l : lignesBon.findByBonCommandeId(bc.getId())) {
            lignesByProduit.put(l.getProduitId(), l);
        }
        return new BonCommandeReceptionContext(bc, lignesByProduit);
    }

    private void majStatutBonCommandeApresReception(BonCommandeJpaEntity bc) {
        boolean allFull = lignesBon.findByBonCommandeId(bc.getId()).stream()
                .allMatch(l -> l.getQuantiteRecue() >= l.getQuantiteCommandee());
        if (allFull) {
            bc.marquerRecuComplet();
        } else {
            bc.marquerRecuPartiel();
        }
    }
}

