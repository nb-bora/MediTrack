package cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategorieProduitJpaRepository extends JpaRepository<CategorieProduitJpaEntity, UUID> {
    boolean existsByOrganisationIdAndParentIdAndNomIgnoreCase(UUID organisationId, UUID parentId, String nom);

    List<CategorieProduitJpaEntity> findByOrganisationIdOrderByNomAsc(UUID organisationId);

    boolean existsByOrganisationIdAndId(UUID organisationId, UUID id);
}

