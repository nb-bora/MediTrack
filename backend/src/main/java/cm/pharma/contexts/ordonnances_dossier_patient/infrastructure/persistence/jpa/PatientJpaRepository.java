package cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PatientJpaRepository extends JpaRepository<PatientJpaEntity, UUID> {
    Optional<PatientJpaEntity> findByOrganisationIdAndId(UUID organisationId, UUID id);

    Optional<PatientJpaEntity> findByOrganisationIdAndTelephone(UUID organisationId, String telephone);

    List<PatientJpaEntity> findByOrganisationIdOrderByNomAscPrenomAsc(UUID organisationId);

    @Query("""
            select p
            from PatientJpaEntity p
            where p.organisationId = :organisationId
              and (
                   upper(p.nom) like upper(concat('%', :q, '%'))
                or upper(p.prenom) like upper(concat('%', :q, '%'))
                or upper(coalesce(p.telephone,'')) like upper(concat('%', :q, '%'))
              )
            order by p.nom asc, p.prenom asc
            """)
    List<PatientJpaEntity> search(UUID organisationId, String q);
}

