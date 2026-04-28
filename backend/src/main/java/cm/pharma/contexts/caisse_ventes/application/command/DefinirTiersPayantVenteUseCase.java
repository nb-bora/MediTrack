package cm.pharma.contexts.caisse_ventes.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.OrganismeAssuranceJpaRepository;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteJpaRepository;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceJpaRepository;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.PatientJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefinirTiersPayantVenteUseCase {

    private final VenteJpaRepository ventes;
    private final PatientJpaRepository patients;
    private final OrganismeAssuranceJpaRepository organismes;
    private final OrdonnanceJpaRepository ordonnances;
    private final AuditWriter auditWriter;

    public DefinirTiersPayantVenteUseCase(
            VenteJpaRepository ventes,
            PatientJpaRepository patients,
            OrganismeAssuranceJpaRepository organismes,
            OrdonnanceJpaRepository ordonnances,
            AuditWriter auditWriter
    ) {
        this.ventes = Objects.requireNonNull(ventes);
        this.patients = Objects.requireNonNull(patients);
        this.organismes = Objects.requireNonNull(organismes);
        this.ordonnances = Objects.requireNonNull(ordonnances);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public void execute(DefinirTiersPayantCommand cmd) {
        Objects.requireNonNull(cmd);
        VenteJpaEntity vente = ventes.findByOrganisationIdAndId(cmd.organisationId(), cmd.venteId())
                .orElseThrow(() -> new BusinessRuleViolationException("Vente introuvable"));
        if (!"BROUILLON".equals(vente.getStatut())) {
            throw new BusinessRuleViolationException("Tiers payant interdit sur ce statut");
        }
        patients.findByOrganisationIdAndId(cmd.organisationId(), cmd.patientId())
                .orElseThrow(() -> new BusinessRuleViolationException("Patient introuvable"));
        organismes.findByOrganisationIdAndId(cmd.organisationId(), cmd.organismeId())
                .orElseThrow(() -> new BusinessRuleViolationException("Organisme introuvable"));
        if (cmd.ordonnanceId() != null) {
            ordonnances.findByOrganisationIdAndId(cmd.organisationId(), cmd.ordonnanceId())
                    .orElseThrow(() -> new BusinessRuleViolationException("Ordonnance introuvable"));
        }

        Instant now = Instant.now();
        vente.setContexteTiersPayant(cmd.patientId(), cmd.organismeId(), cmd.ordonnanceId(), cmd.numeroAdherent());
        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, cmd.actorId(), null, null,
                cmd.posteNom(), null, "VENTE_TIERS_PAYANT_DEFINI", "Vente", vente.getNumeroVente(), null,
                Map.of("patient_id", cmd.patientId(), "organisme_id", cmd.organismeId(), "ordonnance_id", cmd.ordonnanceId())
        ));
    }

    public record DefinirTiersPayantCommand(
            UUID organisationId,
            UUID venteId,
            UUID patientId,
            UUID organismeId,
            UUID ordonnanceId,
            String numeroAdherent,
            UUID actorId,
            String posteNom
    ) {
    }
}

