package cm.pharma.contexts.caisse_ventes.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.caisse_ventes.application.service.TicketRenderer;
import cm.pharma.contexts.caisse_ventes.application.service.TicketRenderer.TicketHeader;
import cm.pharma.contexts.caisse_ventes.application.service.TicketRenderer.TicketLine;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.PaiementVenteJpaRepository;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteJpaRepository;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteLigneJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteLigneJpaRepository;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteLotJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteLotJpaRepository;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaEntity;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaRepository;
import cm.pharma.contexts.assurance_mutuelle.application.command.CreerDossierTiersPayantDepuisVenteUseCase;
import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.EmplacementJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.MouvementStockJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.MouvementStockJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.StockEmplacementJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.StockEmplacementJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.InventaireJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValiderVenteUseCase {

    private static final String EMPLACEMENT_COMPTOIR_MED = "COMPTOIR_MED";
    private static final String EMPLACEMENT_COMPTOIR_PARA = "COMPTOIR_PARA";

    private final VenteJpaRepository ventes;
    private final VenteLigneJpaRepository lignes;
    private final VenteLotJpaRepository lotsVente;
    private final PaiementVenteJpaRepository paiements;
    private final ProduitJpaRepository produits;
    private final EmplacementJpaRepository emplacements;
    private final StockEmplacementJpaRepository stock;
    private final MouvementStockJpaRepository mouvements;
    private final InventaireJpaRepository inventaires;
    private final AuditWriter auditWriter;
    private final CreerDossierTiersPayantDepuisVenteUseCase creerDossierTp;

    public ValiderVenteUseCase(
            VenteJpaRepository ventes,
            VenteLigneJpaRepository lignes,
            VenteLotJpaRepository lotsVente,
            PaiementVenteJpaRepository paiements,
            ProduitJpaRepository produits,
            EmplacementJpaRepository emplacements,
            StockEmplacementJpaRepository stock,
            MouvementStockJpaRepository mouvements,
            InventaireJpaRepository inventaires,
            AuditWriter auditWriter,
            CreerDossierTiersPayantDepuisVenteUseCase creerDossierTp
    ) {
        this.ventes = Objects.requireNonNull(ventes);
        this.lignes = Objects.requireNonNull(lignes);
        this.lotsVente = Objects.requireNonNull(lotsVente);
        this.paiements = Objects.requireNonNull(paiements);
        this.produits = Objects.requireNonNull(produits);
        this.emplacements = Objects.requireNonNull(emplacements);
        this.stock = Objects.requireNonNull(stock);
        this.mouvements = Objects.requireNonNull(mouvements);
        this.inventaires = Objects.requireNonNull(inventaires);
        this.auditWriter = Objects.requireNonNull(auditWriter);
        this.creerDossierTp = Objects.requireNonNull(creerDossierTp);
    }

    @Transactional
    public ValiderVenteResult execute(UUID organisationId, UUID venteId, UUID actorId, String posteNom, boolean peutValiderOrdonnance) {
        inventaires.findOuvert(organisationId).ifPresent(i -> {
            throw new BusinessRuleViolationException("Mouvements bloqués: inventaire ouvert");
        });

        VenteJpaEntity vente = ventes.findByOrganisationIdAndId(organisationId, venteId)
                .orElseThrow(() -> new BusinessRuleViolationException("Vente introuvable"));
        if (!"BROUILLON".equals(vente.getStatut())) {
            throw new BusinessRuleViolationException("Vente non validable");
        }

        List<VenteLigneJpaEntity> lignesVente = lignes.findByVenteId(venteId);
        if (lignesVente.isEmpty()) {
            throw new BusinessRuleViolationException("Vente vide");
        }

        BigDecimal total = lignesVente.stream().map(VenteLigneJpaEntity::getTotalLigne).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRemise = lignesVente.stream().map(VenteLigneJpaEntity::getRemise).reduce(BigDecimal.ZERO, BigDecimal::add);
        vente.setTotals(total, totalRemise);

        BigDecimal totalNet = total.subtract(vente.getArrondi() == null ? BigDecimal.ZERO : vente.getArrondi());
        if (totalNet.compareTo(BigDecimal.ZERO) < 0) {
            totalNet = BigDecimal.ZERO;
        }

        var paiementsVente = paiements.findByVenteId(venteId);
        BigDecimal totalPaye = paiementsVente.stream()
                .map(p -> p.getMontant())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalPaye.compareTo(totalNet) < 0) {
            throw new BusinessRuleViolationException("Paiement insuffisant");
        }
        BigDecimal monnaie = totalPaye.subtract(totalNet);

        Instant now = Instant.now();

        // Allocation lots FEFO par ligne (au comptoir adapté).
        for (VenteLigneJpaEntity l : lignesVente) {
            ProduitJpaEntity produit = produits.findById(l.getProduitId())
                    .orElseThrow(() -> new BusinessRuleViolationException("Produit introuvable"));
            if (!produit.getOrganisationId().equals(organisationId)) {
                throw new BusinessRuleViolationException("Produit hors organisation");
            }
            if (produit.isNecessiteOrdonnance() && !peutValiderOrdonnance) {
                throw new BusinessRuleViolationException("Validation pharmacien requise (produit sur ordonnance)");
            }
            allouerEtDeduirStockParFefo(organisationId, vente, l, actorId, posteNom, now);
        }

        vente.valider(actorId, now);

        UUID dossierTpId = null;
        boolean hasTiersPayant = paiementsVente.stream().anyMatch(p -> "TIERS_PAYANT".equalsIgnoreCase(p.getModePaiement()));
        if (hasTiersPayant) {
            dossierTpId = creerDossierTp.execute(organisationId, venteId, actorId);
        }

        auditWriter.write(AuditEvent.simple(
                organisationId, now, actorId, null, null,
                posteNom, null, "VENTE_VALIDEE", "Vente", vente.getNumeroVente(), null,
                Map.of(
                        "ticket", vente.getNumeroTicket(),
                        "total_ttc", total,
                        "total_remise", totalRemise,
                        "arrondi", vente.getArrondi(),
                        "total_net", totalNet,
                        "paye", totalPaye,
                        "dossier_tiers_payant_id", dossierTpId
                )
        ));

        List<TicketLine> ticketLines = lignesVente.stream()
                .map(l -> {
                    ProduitJpaEntity p = produits.findById(l.getProduitId()).orElse(null);
                    String libelle = p == null ? l.getProduitId().toString() : p.getNomCommercial();
                    return new TicketLine(libelle, l.getQuantite(), l.getTotalLigne());
                })
                .toList();
        String ticket = TicketRenderer.render(
                new TicketHeader(posteNom, vente.getNumeroVente(), vente.getNumeroTicket(), total, vente.getArrondi()),
                ticketLines,
                totalPaye,
                monnaie
        );
        return new ValiderVenteResult(vente.getNumeroVente(), vente.getNumeroTicket(), total, totalRemise, vente.getArrondi(), totalNet, totalPaye, monnaie, ticket);
    }

    private record TraceVenteLotContext(
            UUID organisationId,
            Instant now,
            UUID actorId,
            String posteNom,
            String numeroVente,
            UUID venteLigneId,
            UUID produitId,
            UUID emplacementId
    ) {
    }

    private void allouerEtDeduirStockParFefo(UUID organisationId, VenteJpaEntity vente, VenteLigneJpaEntity l, UUID actorId, String posteNom, Instant now) {
        ProduitJpaEntity produit = produits.findById(l.getProduitId())
                .orElseThrow(() -> new BusinessRuleViolationException("Produit introuvable"));
        if (!produit.getOrganisationId().equals(organisationId)) {
            throw new BusinessRuleViolationException("Produit hors organisation");
        }

        UUID emplacementId = resolveComptoirId(organisationId, produit);
        List<StockEmplacementJpaRepository.StockLotDisponibleRow> lotsDisponibles = stock.findLotsActifsDisponibles(organisationId, emplacementId, l.getProduitId()).stream()
                .sorted(Comparator.comparing(StockEmplacementJpaRepository.StockLotDisponibleRow::getDatePeremption)
                        .thenComparing(StockEmplacementJpaRepository.StockLotDisponibleRow::getNumeroLot))
                .toList();

        int remaining = l.getQuantite();
        for (var row : lotsDisponibles) {
            if (remaining <= 0) {
                break;
            }
            int take = Math.min(row.getQuantite(), remaining);
            deduireStockEtTracerVente(new TraceVenteLotContext(
                    organisationId,
                    now,
                    actorId,
                    posteNom,
                    vente.getNumeroVente(),
                    l.getId(),
                    l.getProduitId(),
                    emplacementId
            ), row.getLotId(), take);
            remaining -= take;
        }
        if (remaining > 0) {
            throw new BusinessRuleViolationException("Stock insuffisant au comptoir (FEFO) pour produit " + l.getProduitId());
        }
    }

    private UUID resolveComptoirId(UUID organisationId, ProduitJpaEntity produit) {
        String comptoirCode = "MEDICAMENT".equalsIgnoreCase(produit.getTypeProduit()) ? EMPLACEMENT_COMPTOIR_MED : EMPLACEMENT_COMPTOIR_PARA;
        return emplacements.findByOrganisationIdAndCode(organisationId, comptoirCode)
                .orElseThrow(() -> new BusinessRuleViolationException("Emplacement comptoir introuvable: " + comptoirCode))
                .getId();
    }

    private void deduireStockEtTracerVente(TraceVenteLotContext ctx, UUID lotId, int quantite) {
        StockEmplacementJpaEntity se = stock.findByOrganisationIdAndEmplacementIdAndLotId(ctx.organisationId(), ctx.emplacementId(), lotId)
                .orElseThrow(() -> new BusinessRuleViolationException("Stock introuvable (concurrence)"));
        int newQty = se.getQuantite() - quantite;
        if (newQty < 0) {
            throw new BusinessRuleViolationException("Stock négatif détecté — recommencer");
        }
        se.setQuantite(newQty, ctx.now());
        stock.save(se);

        lotsVente.save(VenteLotJpaEntity.create(UUID.randomUUID(), ctx.venteLigneId(), ctx.organisationId(), lotId, quantite, ctx.emplacementId()));
        mouvements.save(MouvementStockJpaEntity.create(new MouvementStockJpaEntity.MouvementInit(
                UUID.randomUUID(),
                ctx.organisationId(),
                "VENTE",
                lotId,
                ctx.produitId(),
                quantite,
                ctx.emplacementId(),
                null,
                ctx.numeroVente(),
                null,
                ctx.actorId(),
                ctx.now()
        )));

        auditWriter.write(AuditEvent.simple(
                ctx.organisationId(), ctx.now(), ctx.actorId(), null, null, ctx.posteNom(), null,
                "VENTE_LOT_ALLOUE", "Vente", ctx.numeroVente(), null,
                Map.of("produit_id", ctx.produitId(), "lot_id", lotId, "qte", quantite, "emplacement_id", ctx.emplacementId())
        ));
    }

    public record ValiderVenteResult(
            String numeroVente,
            String numeroTicket,
            BigDecimal totalTtc,
            BigDecimal totalRemise,
            BigDecimal arrondi,
            BigDecimal totalNet,
            BigDecimal totalPaye,
            BigDecimal monnaieRendue,
            String ticketTexte
    ) {
    }
}

