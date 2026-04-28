package cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

public interface PasswordHistoryJpaRepository extends JpaRepository<PasswordHistoryJpaEntity, UUID> {
    List<PasswordHistoryJpaEntity> findTop5ByUtilisateurIdOrderByCreatedAtDesc(UUID utilisateurId);

    List<PasswordHistoryJpaEntity> findByUtilisateurIdOrderByCreatedAtDesc(UUID utilisateurId, Pageable pageable);
}

