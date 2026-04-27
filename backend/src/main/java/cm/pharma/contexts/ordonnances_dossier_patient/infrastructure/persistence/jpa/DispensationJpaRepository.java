package cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispensationJpaRepository extends JpaRepository<DispensationJpaEntity, UUID> {
    List<DispensationJpaEntity> findByOrdonnanceId(UUID ordonnanceId);
}

