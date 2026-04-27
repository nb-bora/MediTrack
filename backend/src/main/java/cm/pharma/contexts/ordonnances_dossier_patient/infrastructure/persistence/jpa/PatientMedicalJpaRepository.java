package cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientMedicalJpaRepository extends JpaRepository<PatientMedicalJpaEntity, UUID> {
    Optional<PatientMedicalJpaEntity> findByPatientId(UUID patientId);
}

