package cm.pharma.contexts.audit_tracabilite.infrastructure.persistence.jpa;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvenementAuditJpaRepository extends JpaRepository<EvenementAuditJpaEntity, UUID> {
}

