package cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrdonnanceJpaRepository extends JpaRepository<OrdonnanceJpaEntity, UUID> {
    Optional<OrdonnanceJpaEntity> findByOrganisationIdAndId(UUID organisationId, UUID id);

    List<OrdonnanceJpaEntity> findByOrganisationIdAndPatientIdOrderByIdDesc(UUID organisationId, UUID patientId);

    @Query("""
            select o
            from OrdonnanceJpaEntity o
            where o.organisationId = :organisationId
              and o.statut = 'PARTIELLEMENT_DISPENSEE'
            order by o.createdAt desc
            """)
    List<OrdonnanceJpaEntity> findPartiellementDispensees(UUID organisationId);

    @Query("""
            select o
            from OrdonnanceJpaEntity o
            where o.organisationId = :organisationId
              and o.statut = 'EN_ATTENTE_VALIDATION'
            order by o.createdAt desc
            """)
    List<OrdonnanceJpaEntity> findEnAttenteValidation(UUID organisationId);

    @Query("""
            select o
            from OrdonnanceJpaEntity o
            where o.organisationId = :organisationId
              and o.statut in ('VALIDEE','PARTIELLEMENT_DISPENSEE','EN_ATTENTE_VALIDATION')
              and o.dateExpiration = :dateExpiration
            """)
    List<OrdonnanceJpaEntity> findAlerteRenouvellementJ7(UUID organisationId, LocalDate dateExpiration);
}

