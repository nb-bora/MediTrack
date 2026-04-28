package cm.pharma.contexts.audit_tracabilite.infrastructure.persistence.jpa;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EvenementAuditJpaRepository extends JpaRepository<EvenementAuditJpaEntity, UUID> {
    List<EvenementAuditJpaEntity> findTop200ByOrganisationIdOrderByHorodatageDesc(UUID organisationId);

    @Query("""
            select e
            from EvenementAuditJpaEntity e
            where e.organisationId = :organisationId
              and (:action is null or e.action = :action)
              and (:entite is null or e.entite = :entite)
              and (:from is null or e.horodatage >= :from)
              and (:to is null or e.horodatage <= :to)
            order by e.horodatage desc
            """)
    List<EvenementAuditJpaEntity> search(
            @Param("organisationId") UUID organisationId,
            @Param("action") String action,
            @Param("entite") String entite,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}

