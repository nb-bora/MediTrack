package cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaiementVenteJpaRepository extends JpaRepository<PaiementVenteJpaEntity, UUID> {
    List<PaiementVenteJpaEntity> findByVenteId(UUID venteId);
}

