package cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.Instant;

public interface BonCommandeJpaRepository extends JpaRepository<BonCommandeJpaEntity, UUID> {
    Optional<BonCommandeJpaEntity> findByOrganisationIdAndId(UUID organisationId, UUID id);

    Optional<BonCommandeJpaEntity> findByOrganisationIdAndNumero(UUID organisationId, String numero);

    List<BonCommandeJpaEntity> findByOrganisationIdOrderByCreatedAtDesc(UUID organisationId);

    @Query("""
            select b
            from BonCommandeJpaEntity b
            where b.organisationId = :organisationId
              and b.fournisseurId = :fournisseurId
              and b.createdAt >= :since
            """)
    List<BonCommandeJpaEntity> findRecentByFournisseur(UUID organisationId, UUID fournisseurId, Instant since);
}

