package cm.pharma.contexts.referentiel.infrastructure.persistence.jpa;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParametreJpaRepository extends JpaRepository<ParametreJpaEntity, UUID> {
    boolean existsByOrganisationIdAndCle(UUID organisationId, String cle);
}

