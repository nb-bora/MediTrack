package cm.pharma.contexts.achats_fournisseurs.application.command;

import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.BonCommandeJpaEntity;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.BonCommandeJpaRepository;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.BonCommandeLigneJpaRepository;
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
public class ValiderBonCommandeUseCase {

    private final BonCommandeJpaRepository bons;
    private final BonCommandeLigneJpaRepository lignes;
    private final AuditWriter auditWriter;

    public ValiderBonCommandeUseCase(BonCommandeJpaRepository bons, BonCommandeLigneJpaRepository lignes, AuditWriter auditWriter) {
        this.bons = Objects.requireNonNull(bons);
        this.lignes = Objects.requireNonNull(lignes);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public void execute(UUID organisationId, UUID bonCommandeId, UUID actorId) {
        BonCommandeJpaEntity bc = bons.findByOrganisationIdAndId(organisationId, bonCommandeId)
                .orElseThrow(() -> new BusinessRuleViolationException("Bon de commande introuvable"));
        if (!"BROUILLON".equals(bc.getStatut())) {
            throw new BusinessRuleViolationException("Bon de commande non validable");
        }
        if (lignes.findByBonCommandeId(bonCommandeId).isEmpty()) {
            throw new BusinessRuleViolationException("Impossible de valider un bon de commande sans lignes");
        }
        Instant now = Instant.now();
        bc.valider(actorId, now);

        auditWriter.write(AuditEvent.simple(
                organisationId, now, null, null, null,
                null, null, "BON_COMMANDE_VALIDE", "BonCommande", bonCommandeId.toString(), null,
                Map.of("numero", bc.getNumero())
        ));
    }
}

