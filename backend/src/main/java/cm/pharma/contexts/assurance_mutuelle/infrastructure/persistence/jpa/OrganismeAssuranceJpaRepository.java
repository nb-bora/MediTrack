package cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganismeAssuranceJpaRepository extends JpaRepository<OrganismeAssuranceJpaEntity, UUID> {
    Optional<OrganismeAssuranceJpaEntity> findByOrganisationIdAndId(UUID organisationId, UUID id);

    boolean existsByOrganisationIdAndCodeIgnoreCase(UUID organisationId, String code);

    List<OrganismeAssuranceJpaEntity> findByOrganisationIdOrderByNomAsc(UUID organisationId);
}

