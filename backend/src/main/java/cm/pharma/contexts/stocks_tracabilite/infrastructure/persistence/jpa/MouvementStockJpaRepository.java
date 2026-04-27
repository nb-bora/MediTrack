package cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MouvementStockJpaRepository extends JpaRepository<MouvementStockJpaEntity, UUID> {
    @Query("""
            select m
            from MouvementStockJpaEntity m
            where m.organisationId = :organisationId
              and (:produitId is null or m.produitId = :produitId)
              and (:lotId is null or m.lotId = :lotId)
              and (:from is null or m.createdAt >= :from)
              and (:to is null or m.createdAt <= :to)
            order by m.createdAt desc
            """)
    List<MouvementStockJpaEntity> search(UUID organisationId, UUID produitId, UUID lotId, Instant from, Instant to);
}

