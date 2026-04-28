package cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CodeBarresProduitJpaRepository extends JpaRepository<CodeBarresProduitJpaEntity, UUID> {
    boolean existsByEan13(String ean13);

    @Query("""
            select (count(c) > 0)
            from CodeBarresProduitJpaEntity c, ProduitJpaEntity p
            where c.produitId = p.id
              and p.organisationId = :organisationId
              and c.ean13 = :ean13
            """)
    boolean existsByOrganisationIdAndEan13(UUID organisationId, String ean13);

    Optional<CodeBarresProduitJpaEntity> findByEan13(String ean13);

    List<CodeBarresProduitJpaEntity> findByProduitId(UUID produitId);
}

