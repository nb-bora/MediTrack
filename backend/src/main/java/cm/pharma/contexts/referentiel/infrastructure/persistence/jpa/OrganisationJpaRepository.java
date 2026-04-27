package cm.pharma.contexts.referentiel.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganisationJpaRepository extends JpaRepository<OrganisationJpaEntity, UUID> {
    boolean existsByNumeroAutorisationOuverture(String numeroAutorisationOuverture);

    Optional<OrganisationJpaEntity> findFirstByOrderByCreatedAtAsc();
}

