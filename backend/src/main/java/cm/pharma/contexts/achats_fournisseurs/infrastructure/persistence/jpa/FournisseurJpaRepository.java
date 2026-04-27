package cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FournisseurJpaRepository extends JpaRepository<FournisseurJpaEntity, UUID> {
    boolean existsByOrganisationIdAndRaisonSocialeIgnoreCase(UUID organisationId, String raisonSociale);

    boolean existsByOrganisationIdAndNumeroRcIgnoreCase(UUID organisationId, String numeroRc);

    boolean existsByOrganisationIdAndNumeroContribuableIgnoreCase(UUID organisationId, String numeroContribuable);

    Optional<FournisseurJpaEntity> findByOrganisationIdAndId(UUID organisationId, UUID id);

    List<FournisseurJpaEntity> findByOrganisationIdOrderByRaisonSocialeAsc(UUID organisationId);
}

