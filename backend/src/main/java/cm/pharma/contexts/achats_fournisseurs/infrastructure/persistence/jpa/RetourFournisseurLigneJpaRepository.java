package cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RetourFournisseurLigneJpaRepository extends JpaRepository<RetourFournisseurLigneJpaEntity, UUID> {
    List<RetourFournisseurLigneJpaEntity> findByRetourId(UUID retourId);
}

