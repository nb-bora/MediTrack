package cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PrixProduitJpaRepository extends JpaRepository<PrixProduitJpaEntity, UUID> {
    @Query("""
            select p
            from PrixProduitJpaEntity p
            where p.produitId = :produitId
              and p.typePrix = :typePrix
              and p.dateFin is null
            order by p.dateDebut desc
            """)
    List<PrixProduitJpaEntity> findActive(UUID produitId, String typePrix);

    @Query("""
            select p
            from PrixProduitJpaEntity p
            where p.produitId = :produitId
              and p.typePrix = :typePrix
              and p.dateDebut <= :at
              and (p.dateFin is null or p.dateFin >= :at)
            order by p.dateDebut desc
            """)
    List<PrixProduitJpaEntity> findApplicableAt(UUID produitId, String typePrix, LocalDate at);
}

