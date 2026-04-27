package cm.pharma.contexts.stocks_tracabilite.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaEntity;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.application.service.FefoAllocator;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.LotStockJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.LotStockJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.MouvementStockJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.MouvementStockJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.StockEmplacementJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.StockEmplacementJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.InventaireJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransfererStockUseCase {

    private final ProduitJpaRepository produits;
    private final LotStockJpaRepository lots;
    private final StockEmplacementJpaRepository stock;
    private final MouvementStockJpaRepository mouvements;
    private final InventaireJpaRepository inventaires;
    private final AuditWriter auditWriter;

    public TransfererStockUseCase(
            ProduitJpaRepository produits,
            LotStockJpaRepository lots,
            StockEmplacementJpaRepository stock,
            MouvementStockJpaRepository mouvements,
            InventaireJpaRepository inventaires,
            AuditWriter auditWriter
    ) {
        this.produits = Objects.requireNonNull(produits);
        this.lots = Objects.requireNonNull(lots);
        this.stock = Objects.requireNonNull(stock);
        this.mouvements = Objects.requireNonNull(mouvements);
        this.inventaires = Objects.requireNonNull(inventaires);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public void execute(TransfererStockCommand cmd) {
        Objects.requireNonNull(cmd);

        inventaires.findOuvert(cmd.organisationId()).ifPresent(i -> {
            throw new BusinessRuleViolationException("Mouvements bloqués: inventaire ouvert");
        });

        if (cmd.emplacementSourceId().equals(cmd.emplacementDestinationId())) {
            throw new BusinessRuleViolationException("Emplacements source et destination identiques");
        }

        ProduitJpaEntity produit = produits.findById(cmd.produitId())
                .orElseThrow(() -> new BusinessRuleViolationException("Produit introuvable"));
        if (!produit.getOrganisationId().equals(cmd.organisationId())) {
            throw new BusinessRuleViolationException("Produit hors organisation");
        }

        List<LotStockJpaEntity> actifs = lots.findActifsParProduit(cmd.organisationId(), cmd.produitId());
        if (actifs.isEmpty()) {
            throw new BusinessRuleViolationException("Aucun lot actif pour ce produit");
        }

        List<UUID> lotIds = actifs.stream().map(LotStockJpaEntity::getId).toList();
        Map<UUID, Integer> qtyByLot = new HashMap<>();
        for (StockEmplacementJpaEntity s : stock.findByOrganisationIdAndEmplacementIdAndLotIdIn(cmd.organisationId(), cmd.emplacementSourceId(), lotIds)) {
            qtyByLot.put(s.getLotId(), s.getQuantite());
        }

        List<FefoAllocator.LotPick> picks = FefoAllocator.allocate(actifs, qtyByLot, cmd.quantite());
        int picked = picks.stream().mapToInt(FefoAllocator.LotPick::quantite).sum();
        if (picked < cmd.quantite()) {
            throw new BusinessRuleViolationException("Stock insuffisant à l’emplacement source");
        }

        Instant now = Instant.now();

        Map<UUID, StockEmplacementJpaEntity> sourceRows = stock.findByOrganisationIdAndEmplacementIdAndLotIdIn(cmd.organisationId(), cmd.emplacementSourceId(),
                        picks.stream().map(FefoAllocator.LotPick::lotId).toList())
                .stream().collect(Collectors.toMap(StockEmplacementJpaEntity::getLotId, r -> r));

        for (FefoAllocator.LotPick pick : picks) {
            StockEmplacementJpaEntity src = sourceRows.get(pick.lotId());
            int newQty = src.getQuantite() - pick.quantite();
            if (newQty < 0) {
                throw new BusinessRuleViolationException("Stock négatif détecté (concurrence) — recommencer");
            }
            src.setQuantite(newQty, now);
            stock.save(src);

            StockEmplacementJpaEntity dst = stock.findByOrganisationIdAndEmplacementIdAndLotId(cmd.organisationId(), cmd.emplacementDestinationId(), pick.lotId())
                    .orElseGet(() -> StockEmplacementJpaEntity.create(UUID.randomUUID(), cmd.organisationId(), cmd.emplacementDestinationId(), pick.lotId(), 0, now));
            dst.setQuantite(dst.getQuantite() + pick.quantite(), now);
            stock.save(dst);

            mouvements.save(MouvementStockJpaEntity.create(
                    new MouvementStockJpaEntity.MouvementInit(
                            UUID.randomUUID(),
                            cmd.organisationId(),
                            "TRANSFERT",
                            pick.lotId(),
                            cmd.produitId(),
                            pick.quantite(),
                            cmd.emplacementSourceId(),
                            cmd.emplacementDestinationId(),
                            cmd.referenceDocument(),
                            cmd.motif(),
                            cmd.creePar(),
                            now
                    )
            )));
        }

        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, null, null, null,
                null, null, "STOCK_TRANSFERE", "Produit", cmd.produitId().toString(), cmd.motif(),
                Map.of(
                        "emplacement_source_id", cmd.emplacementSourceId(),
                        "emplacement_destination_id", cmd.emplacementDestinationId(),
                        "quantite", cmd.quantite()
                )
        ));
    }
}

