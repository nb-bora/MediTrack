package cm.pharma.contexts.referentiel.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteJpaRepository extends JpaRepository<SiteJpaEntity, UUID> {
    List<SiteJpaEntity> findByOrganisationId(UUID organisationId);
}

