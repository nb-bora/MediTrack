package cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceptionFournisseurLigneJpaRepository extends JpaRepository<ReceptionFournisseurLigneJpaEntity, UUID> {
    List<ReceptionFournisseurLigneJpaEntity> findByReceptionId(UUID receptionId);
}

