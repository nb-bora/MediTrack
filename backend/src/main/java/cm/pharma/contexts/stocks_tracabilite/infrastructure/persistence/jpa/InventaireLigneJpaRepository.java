package cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventaireLigneJpaRepository extends JpaRepository<InventaireLigneJpaEntity, UUID> {
    List<InventaireLigneJpaEntity> findByInventaireId(UUID inventaireId);

    Optional<InventaireLigneJpaEntity> findByInventaireIdAndProduitIdAndEmplacementId(UUID inventaireId, UUID produitId, UUID emplacementId);
}

