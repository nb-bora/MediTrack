package cm.pharma.contexts.assurance_mutuelle.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.OrganismeAssuranceJpaEntity;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.OrganismeAssuranceJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreerOrganismeAssuranceUseCase {

    private final OrganismeAssuranceJpaRepository organismes;
    private final AuditWriter auditWriter;

    public CreerOrganismeAssuranceUseCase(OrganismeAssuranceJpaRepository organismes, AuditWriter auditWriter) {
        this.organismes = Objects.requireNonNull(organismes);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public UUID execute(CreerOrganismeCommand cmd) {
        Objects.requireNonNull(cmd);
        if (cmd.code() == null || cmd.code().isBlank() || cmd.nom() == null || cmd.nom().isBlank() || cmd.type() == null || cmd.type().isBlank()) {
            throw new BusinessRuleViolationException("Code/nom/type requis");
        }
        if (organismes.existsByOrganisationIdAndCodeIgnoreCase(cmd.organisationId(), cmd.code().trim())) {
            throw new BusinessRuleViolationException("Organisme déjà existant (code)");
        }
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        organismes.save(OrganismeAssuranceJpaEntity.create(new OrganismeAssuranceJpaEntity.OrganismeInit(
                id,
                cmd.organisationId(),
                cmd.code().trim(),
                cmd.nom().trim(),
                cmd.type().trim(),
                cmd.frequenceFacturation() == null ? "MENSUELLE" : cmd.frequenceFacturation(),
                cmd.delaiPaiementJours() <= 0 ? 45 : cmd.delaiPaiementJours(),
                now
        )));
        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, cmd.actorId(), null, null,
                cmd.posteNom(), null, "ORGANISME_ASSURANCE_CREE", "OrganismeAssurance", id.toString(), null,
                Map.of("code", cmd.code(), "nom", cmd.nom())
        ));
        return id;
    }

    public record CreerOrganismeCommand(
            UUID organisationId,
            String code,
            String nom,
            String type,
            String frequenceFacturation,
            int delaiPaiementJours,
            UUID actorId,
            String posteNom
    ) {
    }
}

