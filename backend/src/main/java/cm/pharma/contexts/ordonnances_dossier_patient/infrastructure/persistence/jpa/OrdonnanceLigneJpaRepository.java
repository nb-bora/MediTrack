package cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdonnanceLigneJpaRepository extends JpaRepository<OrdonnanceLigneJpaEntity, UUID> {
    List<OrdonnanceLigneJpaEntity> findByOrdonnanceId(UUID ordonnanceId);

    Optional<OrdonnanceLigneJpaEntity> findByOrganisationIdAndId(UUID organisationId, UUID id);
}

