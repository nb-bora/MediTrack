package cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfilTaxeJpaRepository extends JpaRepository<ProfilTaxeJpaEntity, UUID> {
    Optional<ProfilTaxeJpaEntity> findByOrganisationIdAndNom(UUID organisationId, String nom);

    boolean existsByOrganisationIdAndId(UUID organisationId, UUID id);
}

