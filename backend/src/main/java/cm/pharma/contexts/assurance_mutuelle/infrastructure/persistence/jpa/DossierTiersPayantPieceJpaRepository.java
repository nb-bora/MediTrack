package cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DossierTiersPayantPieceJpaRepository extends JpaRepository<DossierTiersPayantPieceJpaEntity, UUID> {
    List<DossierTiersPayantPieceJpaEntity> findByOrganisationIdAndDossierId(UUID organisationId, UUID dossierId);

    Optional<DossierTiersPayantPieceJpaEntity> findByOrganisationIdAndId(UUID organisationId, UUID id);

    boolean existsByOrganisationIdAndDossierIdAndTypePieceIgnoreCase(UUID organisationId, UUID dossierId, String typePiece);
}

