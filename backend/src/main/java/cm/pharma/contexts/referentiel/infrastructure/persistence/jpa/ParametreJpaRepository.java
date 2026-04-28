package cm.pharma.contexts.referentiel.infrastructure.persistence.jpa;

import java.util.UUID;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParametreJpaRepository extends JpaRepository<ParametreJpaEntity, UUID> {
    boolean existsByOrganisationIdAndCle(UUID organisationId, String cle);

    Optional<ParametreJpaEntity> findByOrganisationIdAndCle(UUID organisationId, String cle);

    List<ParametreJpaEntity> findByOrganisationIdOrderByCleAsc(UUID organisationId);
}

