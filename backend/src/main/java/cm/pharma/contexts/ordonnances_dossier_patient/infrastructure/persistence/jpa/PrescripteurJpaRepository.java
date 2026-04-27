package cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescripteurJpaRepository extends JpaRepository<PrescripteurJpaEntity, UUID> {
    Optional<PrescripteurJpaEntity> findByOrganisationIdAndId(UUID organisationId, UUID id);

    List<PrescripteurJpaEntity> findByOrganisationIdOrderByNomAsc(UUID organisationId);

    boolean existsByOrganisationIdAndNomIgnoreCaseAndStructureIgnoreCase(UUID organisationId, String nom, String structure);
}

