package cm.pharma.contexts.caisse_ventes.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.RetourVenteJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.RetourVenteJpaRepository;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.RetourVenteLigneJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.RetourVenteLigneJpaRepository;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteJpaRepository;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteLigneJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteLigneJpaRepository;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteLotJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteLotJpaRepository;
import cm.pharma.contexts.referentiel.application.service.NumerotationService;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.LotStockJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.MouvementStockJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.MouvementStockJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.StockEmplacementJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.StockEmplacementJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.InventaireJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreerRetourVenteUseCase {

    private static final Set<String> MODES = Set.of(
            "ESPECES",
            "MOBILE_MONEY_MTN",
            "MOBILE_MONEY_ORANGE",
            "VIREMENT",
            "CHEQUE",
            "AVOIR"
    );

    private final VenteJpaRepository ventes;
    private final VenteLigneJpaRepository lignes;
    private final VenteLotJpaRepository lotsVente;
    private final RetourVenteJpaRepository retours;
    private final RetourVenteLigneJpaRepository retourLignes;
    private final StockEmplacementJpaRepository stock;
    private final LotStockJpaRepository lotsStock;
    private final MouvementStockJpaRepository mouvements;
    private final InventaireJpaRepository inventaires;
    private final NumerotationService numerotation;
    private final AuditWriter auditWriter;

    public CreerRetourVenteUseCase(
            VenteJpaRepository ventes,
            VenteLigneJpaRepository lignes,
            VenteLotJpaRepository lotsVente,
            RetourVenteJpaRepository retours,
            RetourVenteLigneJpaRepository retourLignes,
            StockEmplacementJpaRepository stock,
            LotStockJpaRepository lotsStock,
            MouvementStockJpaRepository mouvements,
            InventaireJpaRepository inventaires,
            NumerotationService numerotation,
            AuditWriter auditWriter
    ) {
        this.ventes = Objects.requireNonNull(ventes);
        this.lignes = Objects.requireNonNull(lignes);
        this.lotsVente = Objects.requireNonNull(lotsVente);
        this.retours = Objects.requireNonNull(retours);
        this.retourLignes = Objects.requireNonNull(retourLignes);
        this.stock = Objects.requireNonNull(stock);
        this.lotsStock = Objects.requireNonNull(lotsStock);
        this.mouvements = Objects.requireNonNull(mouvements);
        this.inventaires = Objects.requireNonNull(inventaires);
        this.numerotation = Objects.requireNonNull(numerotation);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public RetourVenteResult execute(CreerRetourVenteCommand cmd) {
        Objects.requireNonNull(cmd);
        inventaires.findOuvert(cmd.organisationId()).ifPresent(i -> {
            throw new BusinessRuleViolationException("Mouvements bloqués: inventaire ouvert");
        });

        if (!MODES.contains(cmd.modeRemboursement())) {
            throw new BusinessRuleViolationException("Mode remboursement non supporté");
        }
        if (cmd.motif() == null || cmd.motif().isBlank()) {
            throw new BusinessRuleViolationException("Motif obligatoire");
        }
        if (cmd.lignes() == null || cmd.lignes().isEmpty()) {
            throw new BusinessRuleViolationException("Aucune ligne à retourner");
        }

        VenteJpaEntity vente = ventes.findByOrganisationIdAndId(cmd.organisationId(), cmd.venteId())
                .orElseThrow(() -> new BusinessRuleViolationException("Vente introuvable"));
        if (!"VALIDEE".equals(vente.getStatut())) {
            throw new BusinessRuleViolationException("Seule une vente VALIDEE peut être retournée");
        }

        Instant now = Instant.now();
        UUID retourId = UUID.randomUUID();
        String numeroRetour = numerotation.nextNumero(cmd.organisationId(), "RETOUR_VENTE");

        BigDecimal montantTotal = BigDecimal.ZERO;

        RetourContext ctx = new RetourContext(cmd.organisationId(), cmd.venteId(), cmd.motif(), cmd.creePar(), cmd.posteNom(), numeroRetour, retourId, now);
        for (RetourLigne l : cmd.lignes()) {
            montantTotal = montantTotal.add(traiterRetourLigne(ctx, l));
        }

        retours.save(RetourVenteJpaEntity.create(
                retourId,
                cmd.organisationId(),
                cmd.venteId(),
                numeroRetour,
                cmd.motif(),
                cmd.modeRemboursement(),
                cmd.reference(),
                montantTotal,
                cmd.creePar(),
                now
        ));

        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, cmd.creePar(), null, null,
                cmd.posteNom(), null, "RETOUR_VENTE_CREE", "RetourVente", numeroRetour, cmd.motif(),
                Map.of("vente_id", cmd.venteId(), "montant_rembourse", montantTotal, "mode", cmd.modeRemboursement())
        ));

        return new RetourVenteResult(retourId, numeroRetour, montantTotal);
    }

    private record RetourContext(
            UUID organisationId,
            UUID venteId,
            String motif,
            UUID creePar,
            String posteNom,
            String numeroRetour,
            UUID retourId,
            Instant now
    ) {
    }

    private BigDecimal traiterRetourLigne(RetourContext ctx, RetourLigne l) {
        if (l.quantite() <= 0) {
            throw new BusinessRuleViolationException("Quantité retour invalide");
        }
        VenteLigneJpaEntity venteLigne = lignes.findById(l.venteLigneId())
                .orElseThrow(() -> new BusinessRuleViolationException("Ligne introuvable"));
        if (!venteLigne.getVenteId().equals(ctx.venteId())) {
            throw new BusinessRuleViolationException("Ligne hors vente");
        }

        List<VenteLotJpaEntity> lots = lotsVente.findByVenteLigneId(venteLigne.getId());
        if (lots.isEmpty()) {
            throw new BusinessRuleViolationException("Ligne non traçable (aucun lot)");
        }

        BigDecimal montantTotal = BigDecimal.ZERO;
        int remaining = l.quantite();
        for (VenteLotJpaEntity vl : lots) {
            if (remaining <= 0) {
                break;
            }
            int take = Math.min(vl.getQuantite(), remaining);
            montantTotal = montantTotal.add(reintegrerLotEtTracer(ctx, venteLigne, vl, take));
            remaining -= take;
        }
        if (remaining > 0) {
            throw new BusinessRuleViolationException("Quantité retour > quantités vendues/traçées sur la ligne");
        }
        return montantTotal;
    }

    private BigDecimal reintegrerLotEtTracer(RetourContext ctx, VenteLigneJpaEntity venteLigne, VenteLotJpaEntity vl, int quantite) {
        UUID produitId = lotsStock.findById(vl.getLotId())
                .orElseThrow(() -> new BusinessRuleViolationException("Lot introuvable"))
                .getProduitId();

        StockEmplacementJpaEntity se = stock.findByOrganisationIdAndEmplacementIdAndLotId(ctx.organisationId(), vl.getEmplacementId(), vl.getLotId())
                .orElseGet(() -> StockEmplacementJpaEntity.create(UUID.randomUUID(), ctx.organisationId(), vl.getEmplacementId(), vl.getLotId(), 0, ctx.now()));
        se.setQuantite(se.getQuantite() + quantite, ctx.now());
        stock.save(se);

        BigDecimal montantLigne = venteLigne.getPrixUnitaireTtc().multiply(BigDecimal.valueOf(quantite));

        retourLignes.save(RetourVenteLigneJpaEntity.create(
                UUID.randomUUID(),
                ctx.retourId(),
                ctx.organisationId(),
                venteLigne.getId(),
                vl.getLotId(),
                vl.getEmplacementId(),
                quantite,
                montantLigne
        ));

        mouvements.save(MouvementStockJpaEntity.create(new MouvementStockJpaEntity.MouvementInit(
                UUID.randomUUID(),
                ctx.organisationId(),
                "RETOUR_VENTE",
                vl.getLotId(),
                produitId,
                quantite,
                null,
                vl.getEmplacementId(),
                ctx.numeroRetour(),
                ctx.motif(),
                ctx.creePar(),
                ctx.now()
        )));

        return montantLigne;
    }

    public record CreerRetourVenteCommand(
            UUID organisationId,
            UUID venteId,
            String motif,
            String modeRemboursement,
            String reference,
            List<RetourLigne> lignes,
            UUID creePar,
            String posteNom
    ) {
    }

    public record RetourLigne(UUID venteLigneId, int quantite) {
    }

    public record RetourVenteResult(UUID retourVenteId, String numeroRetour, BigDecimal montantRembourse) {
    }
}

