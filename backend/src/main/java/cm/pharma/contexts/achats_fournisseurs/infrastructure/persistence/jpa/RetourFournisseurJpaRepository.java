package cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RetourFournisseurJpaRepository extends JpaRepository<RetourFournisseurJpaEntity, UUID> {
    Optional<RetourFournisseurJpaEntity> findByOrganisationIdAndId(UUID organisationId, UUID id);

    @Query("""
            select r
            from RetourFournisseurJpaEntity r
            where r.organisationId = :organisationId
              and r.fournisseurId = :fournisseurId
              and r.createdAt >= :since
            """)
    List<RetourFournisseurJpaEntity> findRecentByFournisseur(UUID organisationId, UUID fournisseurId, Instant since);
}

