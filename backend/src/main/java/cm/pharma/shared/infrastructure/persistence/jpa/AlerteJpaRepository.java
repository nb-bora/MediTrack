package cm.pharma.shared.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlerteJpaRepository extends JpaRepository<AlerteJpaEntity, UUID> {
    Optional<AlerteJpaEntity> findByOrganisationIdAndTypeAlerteAndEntiteAndEntiteIdAndResolvedAtIsNull(
            UUID organisationId,
            String typeAlerte,
            String entite,
            String entiteId
    );

    List<AlerteJpaEntity> findByOrganisationIdAndResolvedAtIsNullOrderByCreatedAtDesc(UUID organisationId);
}

