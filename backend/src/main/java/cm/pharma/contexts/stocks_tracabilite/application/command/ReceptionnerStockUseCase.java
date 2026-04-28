package cm.pharma.contexts.stocks_tracabilite.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaEntity;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.LotStockJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.LotStockJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.MouvementStockJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.MouvementStockJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.StockEmplacementJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.StockEmplacementJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.InventaireJpaRepository;
import cm.pharma.contexts.referentiel.application.service.ParametresService;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReceptionnerStockUseCase {
    private static final String ENTITY_LOT_STOCK = "LotStock";

    private final ProduitJpaRepository produits;
    private final LotStockJpaRepository lots;
    private final StockEmplacementJpaRepository stock;
    private final MouvementStockJpaRepository mouvements;
    private final InventaireJpaRepository inventaires;
    private final MettreLotEnQuarantaineUseCase mettreEnQuarantaine;
    private final AuditWriter auditWriter;
    private final ParametresService parametres;

    public ReceptionnerStockUseCase(
            ProduitJpaRepository produits,
            LotStockJpaRepository lots,
            StockEmplacementJpaRepository stock,
            MouvementStockJpaRepository mouvements,
            InventaireJpaRepository inventaires,
            MettreLotEnQuarantaineUseCase mettreEnQuarantaine,
            AuditWriter auditWriter,
            ParametresService parametres
    ) {
        this.produits = Objects.requireNonNull(produits);
        this.lots = Objects.requireNonNull(lots);
        this.stock = Objects.requireNonNull(stock);
        this.mouvements = Objects.requireNonNull(mouvements);
        this.inventaires = Objects.requireNonNull(inventaires);
        this.mettreEnQuarantaine = Objects.requireNonNull(mettreEnQuarantaine);
        this.auditWriter = Objects.requireNonNull(auditWriter);
        this.parametres = Objects.requireNonNull(parametres);
    }

    @Transactional
    public UUID execute(ReceptionnerStockCommand cmd) {
        Objects.requireNonNull(cmd);

        inventaires.findOuvert(cmd.organisationId()).ifPresent(i -> {
            throw new BusinessRuleViolationException("Mouvements bloqués: inventaire ouvert");
        });

        ProduitJpaEntity produit = produits.findById(cmd.produitId())
                .orElseThrow(() -> new BusinessRuleViolationException("Produit introuvable"));
        if (!produit.getOrganisationId().equals(cmd.organisationId())) {
            throw new BusinessRuleViolationException("Produit hors organisation");
        }
        if (cmd.datePeremption().isBefore(LocalDate.now())) {
            throw new BusinessRuleViolationException("Impossible de réceptionner un lot déjà périmé");
        }

        int alerteMois = Math.max(0, parametres.getInt(cmd.organisationId(), "RECEPTION_ALERTE_PEREMPTION_MOIS", 6));
        if (Period.between(LocalDate.now(), cmd.datePeremption()).toTotalMonths() < alerteMois && !cmd.confirmerPeremptionProche()) {
            throw new BusinessRuleViolationException("Lot périme dans moins de " + alerteMois + " mois: confirmation pharmacien requise");
        }

        Instant now = Instant.now();
        UUID lotId = lots.findByOrganisationIdAndProduitIdAndNumeroLot(cmd.organisationId(), cmd.produitId(), cmd.numeroLot().trim())
                .map(LotStockJpaEntity::getId)
                .orElseGet(() -> {
                    UUID id = UUID.randomUUID();
                    lots.save(LotStockJpaEntity.create(
                            id,
                            cmd.organisationId(),
                            cmd.produitId(),
                            cmd.numeroLot().trim(),
                            cmd.datePeremption(),
                            "ACTIF",
                            null,
                            cmd.creePar(),
                            now
                    ));
                    return id;
                });

        StockEmplacementJpaEntity se = stock.findByOrganisationIdAndEmplacementIdAndLotId(cmd.organisationId(), cmd.emplacementDestinationId(), lotId)
                .orElseGet(() -> StockEmplacementJpaEntity.create(UUID.randomUUID(), cmd.organisationId(), cmd.emplacementDestinationId(), lotId, 0, now));
        se.setQuantite(se.getQuantite() + cmd.quantite(), now);
        stock.save(se);

        UUID mouvementId = UUID.randomUUID();
        mouvements.save(MouvementStockJpaEntity.create(new MouvementStockJpaEntity.MouvementInit(
                mouvementId,
                cmd.organisationId(),
                "RECEPTION",
                lotId,
                cmd.produitId(),
                cmd.quantite(),
                null,
                cmd.emplacementDestinationId(),
                cmd.referenceDocument(),
                cmd.motif(),
                cmd.creePar(),
                now
        )));

        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, null, null, null,
                null, null, "STOCK_RECEPTIONNE", ENTITY_LOT_STOCK, lotId.toString(), cmd.motif(),
                Map.ofEntries(
                        Map.entry("produit_id", cmd.produitId()),
                        Map.entry("numero_lot", cmd.numeroLot()),
                        Map.entry("date_peremption", cmd.datePeremption()),
                        Map.entry("quantite", cmd.quantite()),
                        Map.entry("emplacement_destination_id", cmd.emplacementDestinationId()),
                        Map.entry("reference_document", cmd.referenceDocument()),
                        Map.entry("prix_achat_unitaire", cmd.prixAchatUnitaire()),
                        Map.entry("quantite_commandee", cmd.quantiteCommandee()),
                        Map.entry("prix_attendu_unitaire", cmd.prixAttenduUnitaire()),
                        Map.entry("prix_facture_unitaire", cmd.prixFactureUnitaire()),
                        Map.entry("temperature_transport_c", cmd.temperatureTransportC()),
                        Map.entry("confirmer_peremption_proche", cmd.confirmerPeremptionProche())
                )
        ));

        // Anomalies de réception (V1): on trace en audit.
        if (cmd.quantiteCommandee() != null && cmd.quantite() < cmd.quantiteCommandee()) {
            auditWriter.write(AuditEvent.simple(
                    cmd.organisationId(), now, null, null, null,
                    null, null, "RECEPTION_ANOMALIE_QTE", ENTITY_LOT_STOCK, lotId.toString(), cmd.motif(),
                    Map.of("quantite_commandee", cmd.quantiteCommandee(), "quantite_recue", cmd.quantite())
            ));
        }
        if (cmd.prixAttenduUnitaire() != null && cmd.prixFactureUnitaire() != null
                && cmd.prixAttenduUnitaire().compareTo(cmd.prixFactureUnitaire()) != 0) {
            auditWriter.write(AuditEvent.simple(
                    cmd.organisationId(), now, null, null, null,
                    null, null, "RECEPTION_ANOMALIE_PRIX", ENTITY_LOT_STOCK, lotId.toString(), cmd.motif(),
                    Map.of("prix_attendu_unitaire", cmd.prixAttenduUnitaire(), "prix_facture_unitaire", cmd.prixFactureUnitaire())
            ));
        }

        // Chaîne du froid : si température fournie hors plage, mise en quarantaine automatique.
        if (cmd.temperatureTransportC() != null) {
            double t = cmd.temperatureTransportC();
            double tMin = parametres.getDouble(cmd.organisationId(), "RECEPTION_CHAINE_FROID_TEMP_MIN_C", 2.0);
            double tMax = parametres.getDouble(cmd.organisationId(), "RECEPTION_CHAINE_FROID_TEMP_MAX_C", 8.0);
            if (t < tMin || t > tMax) {
                mettreEnQuarantaine.execute(cmd.organisationId(), lotId, "CHAINE_DU_FROID_HORS_NORME (" + t + "°C)", cmd.creePar());
            }
        }

        return lotId;
    }
}

