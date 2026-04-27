package cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurJpaRepository extends JpaRepository<UtilisateurJpaEntity, UUID> {
    Optional<UtilisateurJpaEntity> findByLogin(String login);
}

