package cm.pharma.contexts.referentiel.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmplacementJpaRepository extends JpaRepository<EmplacementJpaEntity, UUID> {
    List<EmplacementJpaEntity> findBySiteId(UUID siteId);

    @Query("""
            select e
            from EmplacementJpaEntity e, SiteJpaEntity s
            where e.siteId = s.id
              and s.organisationId = :organisationId
            order by s.nom asc, e.ordreAffichage asc, e.code asc
            """)
    List<EmplacementJpaEntity> findByOrganisationIdOrderBySiteNomAndOrdre(UUID organisationId);

    @Query("""
            select e
            from EmplacementJpaEntity e, SiteJpaEntity s
            where e.siteId = s.id
              and s.organisationId = :organisationId
              and e.code = :code
            """)
    Optional<EmplacementJpaEntity> findByOrganisationIdAndCode(UUID organisationId, String code);
}

