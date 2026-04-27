package cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionAuthJpaRepository extends JpaRepository<SessionAuthJpaEntity, UUID> {
    Optional<SessionAuthJpaEntity> findFirstByRefreshTokenHash(String refreshTokenHash);

    long countByUtilisateurIdAndRevokeeLeIsNullAndExpireLeAfter(UUID utilisateurId, Instant now);
}

