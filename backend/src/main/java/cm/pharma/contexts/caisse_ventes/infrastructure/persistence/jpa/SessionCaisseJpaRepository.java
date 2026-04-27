package cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SessionCaisseJpaRepository extends JpaRepository<SessionCaisseJpaEntity, UUID> {
    @Query("""
            select s
            from SessionCaisseJpaEntity s
            where s.organisationId = :organisationId
              and s.posteNom = :posteNom
              and s.statut = 'OUVERTE'
            """)
    Optional<SessionCaisseJpaEntity> findOuverteParPoste(UUID organisationId, String posteNom);
}

