package cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenteLigneJpaRepository extends JpaRepository<VenteLigneJpaEntity, UUID> {
    List<VenteLigneJpaEntity> findByVenteId(UUID venteId);

    Optional<VenteLigneJpaEntity> findByVenteIdAndProduitId(UUID venteId, UUID produitId);
}

