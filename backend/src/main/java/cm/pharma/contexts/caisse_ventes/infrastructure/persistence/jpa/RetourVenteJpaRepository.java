package cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RetourVenteJpaRepository extends JpaRepository<RetourVenteJpaEntity, UUID> {
}

