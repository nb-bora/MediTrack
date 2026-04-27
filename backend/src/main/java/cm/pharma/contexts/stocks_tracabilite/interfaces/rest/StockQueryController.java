package cm.pharma.contexts.stocks_tracabilite.interfaces.rest;

import cm.pharma.contexts.stocks_tracabilite.application.service.FefoAllocator;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.MouvementStockJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.MouvementStockJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.StockEmplacementJpaRepository;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stocks")
public class StockQueryController {

    private final StockEmplacementJpaRepository stock;
    private final MouvementStockJpaRepository mouvements;

    public StockQueryController(StockEmplacementJpaRepository stock, MouvementStockJpaRepository mouvements) {
        this.stock = stock;
        this.mouvements = mouvements;
    }

    @GetMapping("/mouvements")
    @PreAuthorize("isAuthenticated()")
    public List<MouvementItem> searchMouvements(
            UUID produitId,
            UUID lotId,
            Instant from,
            Instant to,
            JwtAuthenticationToken auth
    ) {
        UUID orgId = OrganisationContext.organisationId(auth);
        return mouvements.search(orgId, produitId, lotId, from, to).stream()
                .map(m -> new MouvementItem(m))
                .toList();
    }

    @GetMapping("/emplacements/{emplacementId}/produits/{produitId}/lots")
    @PreAuthorize("isAuthenticated()")
    public List<LotDisponibleItem> lotsDisponibles(
            @PathVariable UUID emplacementId,
            @PathVariable UUID produitId,
            JwtAuthenticationToken auth
    ) {
        UUID orgId = OrganisationContext.organisationId(auth);
        return stock.findLotsActifsDisponibles(orgId, emplacementId, produitId).stream()
                .sorted(Comparator.comparing(StockEmplacementJpaRepository.StockLotDisponibleRow::getDatePeremption)
                        .thenComparing(StockEmplacementJpaRepository.StockLotDisponibleRow::getNumeroLot))
                .map(r -> new LotDisponibleItem(r.getLotId(), r.getNumeroLot(), r.getDatePeremption(), r.getQuantite()))
                .toList();
    }

    @PostMapping("/allocations/fefo")
    @PreAuthorize("isAuthenticated()")
    public AllocationFefoResponse allocate(@Valid @RequestBody AllocationFefoRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        var rows = stock.findLotsActifsDisponibles(orgId, req.emplacementId(), req.produitId());
        // V1: on produit le plan FEFO à partir des lots disponibles triés.
        var sorted = rows.stream()
                .sorted(Comparator.comparing(StockEmplacementJpaRepository.StockLotDisponibleRow::getDatePeremption)
                        .thenComparing(StockEmplacementJpaRepository.StockLotDisponibleRow::getNumeroLot))
                .toList();
        int remaining = req.quantite();
        java.util.ArrayList<FefoAllocator.LotPick> picks = new java.util.ArrayList<>();
        for (var r : sorted) {
            if (remaining <= 0) {
                break;
            }
            int take = Math.min(r.getQuantite(), remaining);
            picks.add(new FefoAllocator.LotPick(r.getLotId(), take));
            remaining -= take;
        }
        return new AllocationFefoResponse(picks);
    }

    @GetMapping("/lots/{lotId}/localisation")
    @PreAuthorize("isAuthenticated()")
    public List<LocalisationItem> localisation(@PathVariable UUID lotId, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        return stock.localiserLot(orgId, lotId).stream()
                .map(r -> new LocalisationItem(r.getEmplacementId(), r.getQuantite() == null ? 0 : r.getQuantite()))
                .toList();
    }

    public record MouvementItem(
            UUID id,
            String typeMouvement,
            UUID lotId,
            UUID produitId,
            int quantite,
            UUID emplacementSourceId,
            UUID emplacementDestinationId,
            String referenceDocument,
            String motif,
            UUID creePar,
            Instant createdAt
    ) {
        public MouvementItem(MouvementStockJpaEntity m) {
            this(
                    m.getId(),
                    m.getTypeMouvement(),
                    m.getLotId(),
                    m.getProduitId(),
                    m.getQuantite(),
                    m.getEmplacementSourceId(),
                    m.getEmplacementDestinationId(),
                    m.getReferenceDocument(),
                    m.getMotif(),
                    m.getCreePar(),
                    m.getCreatedAt()
            );
        }
    }

    public record LotDisponibleItem(UUID lotId, String numeroLot, java.time.LocalDate datePeremption, int quantite) {
    }

    public record AllocationFefoRequest(
            @NotNull UUID produitId,
            @NotNull UUID emplacementId,
            @Min(1) int quantite
    ) {
    }

    public record AllocationFefoResponse(List<FefoAllocator.LotPick> picks) {
    }

    public record LocalisationItem(UUID emplacementId, int quantite) {
    }
}

