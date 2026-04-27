package cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LotStockJpaRepository extends JpaRepository<LotStockJpaEntity, UUID> {
    Optional<LotStockJpaEntity> findByOrganisationIdAndProduitIdAndNumeroLot(UUID organisationId, UUID produitId, String numeroLot);

    @Query("""
            select l
            from LotStockJpaEntity l
            where l.organisationId = :organisationId
              and l.produitId = :produitId
              and l.statut = 'ACTIF'
            """)
    List<LotStockJpaEntity> findActifsParProduit(UUID organisationId, UUID produitId);

    @Query("""
            select l
            from LotStockJpaEntity l
            where l.organisationId = :organisationId
              and l.datePeremption < :today
            """)
    List<LotStockJpaEntity> findActifsPerimes(UUID organisationId, LocalDate today);

    @Query("""
            select l
            from LotStockJpaEntity l
            where l.organisationId = :organisationId
              and l.statut in ('ACTIF','PEREMPTION_PRECOCE','PEREMPTION_URGENTE')
              and l.datePeremption <= :threshold
            """)
    List<LotStockJpaEntity> findLotsAReevaluerPeremption(UUID organisationId, LocalDate threshold);
}

