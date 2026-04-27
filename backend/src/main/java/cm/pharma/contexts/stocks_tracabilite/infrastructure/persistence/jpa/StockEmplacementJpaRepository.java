package cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StockEmplacementJpaRepository extends JpaRepository<StockEmplacementJpaEntity, UUID> {
    interface StockTheoriqueRow {
        UUID getProduitId();

        UUID getEmplacementId();

        Integer getQuantite();
    }

    interface StockLotDisponibleRow {
        UUID getLotId();

        Integer getQuantite();

        java.time.LocalDate getDatePeremption();

        String getNumeroLot();
    }

    interface LocalisationLotRow {
        UUID getEmplacementId();

        Integer getQuantite();
    }

    Optional<StockEmplacementJpaEntity> findByOrganisationIdAndEmplacementIdAndLotId(UUID organisationId, UUID emplacementId, UUID lotId);

    @Query("""
            select s
            from StockEmplacementJpaEntity s
            where s.organisationId = :organisationId
              and s.lotId = :lotId
              and s.quantite > 0
            """)
    List<StockEmplacementJpaEntity> findDisponiblesParLot(UUID organisationId, UUID lotId);

    @Query("""
            select s
            from StockEmplacementJpaEntity s
            where s.organisationId = :organisationId
              and s.lotId = :lotId
            """)
    List<StockEmplacementJpaEntity> findByOrganisationIdAndLotId(UUID organisationId, UUID lotId);

    @Query("""
            select s
            from StockEmplacementJpaEntity s
            where s.organisationId = :organisationId
              and s.emplacementId = :emplacementId
              and s.lotId in :lotIds
            """)
    List<StockEmplacementJpaEntity> findByOrganisationIdAndEmplacementIdAndLotIdIn(UUID organisationId, UUID emplacementId, List<UUID> lotIds);

    @Query("""
            select s
            from StockEmplacementJpaEntity s
            where s.organisationId = :organisationId
              and s.emplacementId = :emplacementId
              and s.quantite > 0
            """)
    List<StockEmplacementJpaEntity> findDisponiblesParEmplacement(UUID organisationId, UUID emplacementId);

    @Query("""
            select l.produitId as produitId, s.emplacementId as emplacementId, sum(s.quantite) as quantite
            from StockEmplacementJpaEntity s, LotStockJpaEntity l
            where s.organisationId = :organisationId
              and l.id = s.lotId
              and l.statut = 'ACTIF'
            group by l.produitId, s.emplacementId
            """)
    List<StockTheoriqueRow> computeStockTheorique(UUID organisationId);

    @Query("""
            select s.lotId as lotId, s.quantite as quantite, l.datePeremption as datePeremption, l.numeroLot as numeroLot
            from StockEmplacementJpaEntity s, LotStockJpaEntity l
            where s.organisationId = :organisationId
              and s.emplacementId = :emplacementId
              and l.id = s.lotId
              and l.produitId = :produitId
              and l.statut = 'ACTIF'
              and s.quantite > 0
            """)
    List<StockLotDisponibleRow> findLotsActifsDisponibles(UUID organisationId, UUID emplacementId, UUID produitId);

    @Query("""
            select s.emplacementId as emplacementId, sum(s.quantite) as quantite
            from StockEmplacementJpaEntity s
            where s.organisationId = :organisationId
              and s.lotId = :lotId
            group by s.emplacementId
            """)
    List<LocalisationLotRow> localiserLot(UUID organisationId, UUID lotId);
}

