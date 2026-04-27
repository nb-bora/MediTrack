package cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenteLotJpaRepository extends JpaRepository<VenteLotJpaEntity, UUID> {
    List<VenteLotJpaEntity> findByVenteLigneId(UUID venteLigneId);

    List<VenteLotJpaEntity> findByOrganisationIdAndVenteLigneIdIn(UUID organisationId, List<UUID> venteLigneIds);
}

