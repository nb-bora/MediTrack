package cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenteJpaRepository extends JpaRepository<VenteJpaEntity, UUID> {
    Optional<VenteJpaEntity> findByOrganisationIdAndId(UUID organisationId, UUID id);
}

