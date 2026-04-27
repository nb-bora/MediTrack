package cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProduitJpaRepository extends JpaRepository<ProduitJpaEntity, UUID> {
    @Query("""
            select p
            from ProduitJpaEntity p
            where p.organisationId = :organisationId
              and (:q is null or lower(p.nomCommercial) like lower(concat('%', :q, '%'))
                   or lower(p.dci) like lower(concat('%', :q, '%')))
            order by p.nomCommercial asc
            """)
    List<ProduitJpaEntity> search(UUID organisationId, String q);

    List<ProduitJpaEntity> findByOrganisationIdAndDciIgnoreCaseAndDosageIgnoreCase(UUID organisationId, String dci, String dosage);
}

