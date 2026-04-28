package cm.pharma.contexts.referentiel.infrastructure.persistence.jpa;

import java.util.UUID;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SequenceNumerotationJpaRepository extends JpaRepository<SequenceNumerotationJpaEntity, UUID> {
    boolean existsByOrganisationIdAndTypeDocument(UUID organisationId, String typeDocument);

    Optional<SequenceNumerotationJpaEntity> findByOrganisationIdAndTypeDocument(UUID organisationId, String typeDocument);

    List<SequenceNumerotationJpaEntity> findByOrganisationIdOrderByTypeDocumentAsc(UUID organisationId);
}

