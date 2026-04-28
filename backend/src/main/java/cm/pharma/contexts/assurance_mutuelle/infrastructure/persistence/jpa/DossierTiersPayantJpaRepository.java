package cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DossierTiersPayantJpaRepository extends JpaRepository<DossierTiersPayantJpaEntity, UUID> {
    Optional<DossierTiersPayantJpaEntity> findByOrganisationIdAndId(UUID organisationId, UUID id);

    List<DossierTiersPayantJpaEntity> findByOrganisationIdAndStatutOrderByCreatedAtDesc(UUID organisationId, String statut);

    @Query("""
            select d.motifRejet as motif, count(d.id) as nb
            from DossierTiersPayantJpaEntity d
            where d.organisationId = :organisationId
              and d.statut = 'REJETE'
            group by d.motifRejet
            order by nb desc
            """)
    List<MotifRejetCountRow> statsMotifsRejet(UUID organisationId);

    interface MotifRejetCountRow {
        String getMotif();

        long getNb();
    }
}

