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
public class ModifierFournisseurUseCase {

    private final FournisseurJpaRepository fournisseurs;
    private final AuditWriter auditWriter;

    public ModifierFournisseurUseCase(FournisseurJpaRepository fournisseurs, AuditWriter auditWriter) {
        this.fournisseurs = Objects.requireNonNull(fournisseurs);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public void execute(UUID organisationId, UUID fournisseurId, CreerFournisseurCommand cmd, UUID actorId) {
        FournisseurJpaEntity f = fournisseurs.findByOrganisationIdAndId(organisationId, fournisseurId)
                .orElseThrow(() -> new BusinessRuleViolationException("Fournisseur introuvable"));

        String raison = cmd.raisonSociale().trim();
        if (!raison.equalsIgnoreCase(f.getRaisonSociale())
                && fournisseurs.existsByOrganisationIdAndRaisonSocialeIgnoreCase(organisationId, raison)) {
            throw new BusinessRuleViolationException("Doublon fournisseur (raison sociale)");
        }
        if (cmd.numeroRc() != null && !cmd.numeroRc().isBlank()) {
            String rc = cmd.numeroRc().trim();
            String current = f.getNumeroRc();
            boolean changed = current == null || !current.equalsIgnoreCase(rc);
            if (changed && fournisseurs.existsByOrganisationIdAndNumeroRcIgnoreCase(organisationId, rc)) {
                throw new BusinessRuleViolationException("Doublon fournisseur (RC)");
            }
        }
        if (cmd.numeroContribuable() != null && !cmd.numeroContribuable().isBlank()) {
            String nc = cmd.numeroContribuable().trim();
            String current = f.getNumeroContribuable();
            boolean changed = current == null || !current.equalsIgnoreCase(nc);
            if (changed && fournisseurs.existsByOrganisationIdAndNumeroContribuableIgnoreCase(organisationId, nc)) {
                throw new BusinessRuleViolationException("Doublon fournisseur (N° contribuable)");
            }
        }

        Instant now = Instant.now();
        f.update(
                raison,
                cmd.numeroRc(),
                cmd.numeroContribuable(),
                cmd.adresse(),
                cmd.contactNom(),
                cmd.contactTelephone(),
                cmd.emailCommandes(),
                cmd.modePaiementPrefere(),
                actorId,
                now
        );

        auditWriter.write(AuditEvent.simple(
                organisationId, now, null, null, null,
                null, null, "FOURNISSEUR_MODIFIE", "Fournisseur", fournisseurId.toString(), null,
                Map.of("raison_sociale", raison)
        ));
    }
}

