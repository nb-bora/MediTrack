package cm.pharma.contexts.achats_fournisseurs.application.command;

import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.FournisseurJpaEntity;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.FournisseurJpaRepository;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreerFournisseurUseCase {

    private final FournisseurJpaRepository fournisseurs;
    private final AuditWriter auditWriter;

    public CreerFournisseurUseCase(FournisseurJpaRepository fournisseurs, AuditWriter auditWriter) {
        this.fournisseurs = Objects.requireNonNull(fournisseurs);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public UUID execute(CreerFournisseurCommand cmd) {
        Objects.requireNonNull(cmd);
        String raison = cmd.raisonSociale().trim();
        if (raison.isBlank()) {
            throw new BusinessRuleViolationException("Raison sociale requise");
        }
        if (fournisseurs.existsByOrganisationIdAndRaisonSocialeIgnoreCase(cmd.organisationId(), raison)) {
            throw new BusinessRuleViolationException("Fournisseur déjà existant (raison sociale)");
        }
        if (cmd.numeroRc() != null && !cmd.numeroRc().isBlank()
                && fournisseurs.existsByOrganisationIdAndNumeroRcIgnoreCase(cmd.organisationId(), cmd.numeroRc().trim())) {
            throw new BusinessRuleViolationException("Fournisseur déjà existant (RC)");
        }
        if (cmd.numeroContribuable() != null && !cmd.numeroContribuable().isBlank()
                && fournisseurs.existsByOrganisationIdAndNumeroContribuableIgnoreCase(cmd.organisationId(), cmd.numeroContribuable().trim())) {
            throw new BusinessRuleViolationException("Fournisseur déjà existant (N° contribuable)");
        }

        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        fournisseurs.save(FournisseurJpaEntity.create(
                id,
                cmd.organisationId(),
                raison,
                cmd.numeroRc(),
                cmd.numeroContribuable(),
                cmd.adresse(),
                cmd.contactNom(),
                cmd.contactTelephone(),
                cmd.emailCommandes(),
                cmd.modePaiementPrefere(),
                cmd.creePar(),
                now
        ));

        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, null, null, null,
                null, null, "FOURNISSEUR_CREE", "Fournisseur", id.toString(), null,
                Map.of("raison_sociale", raison)
        ));

        return id;
    }
}

