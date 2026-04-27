package cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RappelLotJpaRepository extends JpaRepository<RappelLotJpaEntity, UUID> {
    Optional<RappelLotJpaEntity> findByOrganisationIdAndLotId(UUID organisationId, UUID lotId);
}

