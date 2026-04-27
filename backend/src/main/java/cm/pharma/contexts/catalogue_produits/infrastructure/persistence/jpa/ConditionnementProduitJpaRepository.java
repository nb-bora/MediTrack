package cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConditionnementProduitJpaRepository extends JpaRepository<ConditionnementProduitJpaEntity, UUID> {
    List<ConditionnementProduitJpaEntity> findByProduitId(UUID produitId);

    boolean existsByProduitIdAndNomIgnoreCase(UUID produitId, String nom);
}

