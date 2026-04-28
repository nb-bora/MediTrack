package cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurJpaRepository extends JpaRepository<UtilisateurJpaEntity, UUID> {
    Optional<UtilisateurJpaEntity> findByLogin(String login);

    Optional<UtilisateurJpaEntity> findByOrganisationIdAndLogin(UUID organisationId, String login);

    Optional<UtilisateurJpaEntity> findByOrganisationIdAndId(UUID organisationId, UUID id);

    List<UtilisateurJpaEntity> findByOrganisationIdOrderByNomAscPrenomAsc(UUID organisationId);
}

