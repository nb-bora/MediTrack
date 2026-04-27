package cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, UUID> {
    Optional<RoleJpaEntity> findByOrganisationIdAndCode(UUID organisationId, String code);
}

