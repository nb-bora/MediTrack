package cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordHistoryJpaRepository extends JpaRepository<PasswordHistoryJpaEntity, UUID> {
    List<PasswordHistoryJpaEntity> findTop5ByUtilisateurIdOrderByCreatedAtDesc(UUID utilisateurId);
}

