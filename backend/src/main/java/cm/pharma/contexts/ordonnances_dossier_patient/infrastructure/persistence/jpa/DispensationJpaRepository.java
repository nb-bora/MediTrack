package cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DispensationJpaRepository extends JpaRepository<DispensationJpaEntity, UUID> {
    List<DispensationJpaEntity> findByOrdonnanceId(UUID ordonnanceId);

    List<DispensationJpaEntity> findByOrganisationIdAndOrdonnanceId(UUID organisationId, UUID ordonnanceId);

    @Query("""
            select d
            from DispensationJpaEntity d
            where d.organisationId = :organisationId
              and d.ordonnanceId in (
                    select o.id
                    from OrdonnanceJpaEntity o
                    where o.organisationId = :organisationId
                      and o.patientId = :patientId
              )
            order by d.createdAt desc
            """)
    List<DispensationJpaEntity> findHistoriquePatient(UUID organisationId, UUID patientId);
}

