package cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientJpaRepository extends JpaRepository<PatientJpaEntity, UUID> {
    Optional<PatientJpaEntity> findByOrganisationIdAndId(UUID organisationId, UUID id);

    Optional<PatientJpaEntity> findByOrganisationIdAndTelephone(UUID organisationId, String telephone);

    List<PatientJpaEntity> findByOrganisationIdOrderByNomAscPrenomAsc(UUID organisationId);
}

