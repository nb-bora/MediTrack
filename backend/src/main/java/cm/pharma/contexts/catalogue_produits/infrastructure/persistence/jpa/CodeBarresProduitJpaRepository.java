package cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeBarresProduitJpaRepository extends JpaRepository<CodeBarresProduitJpaEntity, UUID> {
    boolean existsByEan13(String ean13);

    Optional<CodeBarresProduitJpaEntity> findByEan13(String ean13);

    List<CodeBarresProduitJpaEntity> findByProduitId(UUID produitId);
}

