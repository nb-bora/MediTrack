package cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProduitSubstitutJpaRepository extends JpaRepository<ProduitSubstitutJpaEntity, ProduitSubstitutId> {
    boolean existsById(ProduitSubstitutId id);
}

