package cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InventaireJpaRepository extends JpaRepository<InventaireJpaEntity, UUID> {
    @Query("""
            select i
            from InventaireJpaEntity i
            where i.organisationId = :organisationId
              and i.statut = 'OUVERT'
            """)
    Optional<InventaireJpaEntity> findOuvert(UUID organisationId);
}

