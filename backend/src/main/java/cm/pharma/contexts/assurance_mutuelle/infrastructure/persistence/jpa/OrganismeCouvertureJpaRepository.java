package cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganismeCouvertureJpaRepository extends JpaRepository<OrganismeCouvertureJpaEntity, UUID> {
    Optional<OrganismeCouvertureJpaEntity> findByOrganisationIdAndOrganismeId(UUID organisationId, UUID organismeId);
}

