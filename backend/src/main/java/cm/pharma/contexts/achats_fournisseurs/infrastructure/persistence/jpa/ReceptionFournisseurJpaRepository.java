package cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReceptionFournisseurJpaRepository extends JpaRepository<ReceptionFournisseurJpaEntity, UUID> {
    @Query("""
            select r
            from ReceptionFournisseurJpaEntity r
            where r.organisationId = :organisationId
              and r.id = :id
            """)
    Optional<ReceptionFournisseurJpaEntity> findByOrganisationIdAndId(UUID organisationId, UUID id);

    @Query("""
            select r
            from ReceptionFournisseurJpaEntity r
            where r.organisationId = :organisationId
              and r.fournisseurId = :fournisseurId
              and r.createdAt >= :since
            """)
    List<ReceptionFournisseurJpaEntity> findRecentByFournisseur(UUID organisationId, UUID fournisseurId, Instant since);
}

