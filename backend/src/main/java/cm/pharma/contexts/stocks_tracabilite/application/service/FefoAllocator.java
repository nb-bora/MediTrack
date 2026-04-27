package cm.pharma.contexts.stocks_tracabilite.application.service;

import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.LotStockJpaEntity;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class FefoAllocator {
    private FefoAllocator() {
    }

    public record LotPick(UUID lotId, int quantite) {
    }

    /**
     * Applique FEFO (First Expired, First Out) :
     * lots triés par date de péremption croissante.
     */
    public static List<LotPick> allocate(List<LotStockJpaEntity> lots, Map<UUID, Integer> qtyAvailableByLot, int requestedQty) {
        List<LotStockJpaEntity> sorted = lots.stream()
                .sorted(Comparator.comparing(LotStockJpaEntity::getDatePeremption).thenComparing(LotStockJpaEntity::getNumeroLot))
                .toList();
        java.util.ArrayList<LotPick> picks = new java.util.ArrayList<>();
        int remaining = requestedQty;
        for (LotStockJpaEntity lot : sorted) {
            int avail = qtyAvailableByLot.getOrDefault(lot.getId(), 0);
            if (remaining > 0 && avail > 0) {
                int take = Math.min(avail, remaining);
                picks.add(new LotPick(lot.getId(), take));
                remaining -= take;
            }
        }
        return picks;
    }
}

