package cm.pharma.contexts.achats_fournisseurs.application.command;

import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.BonCommandeJpaEntity;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.BonCommandeJpaRepository;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.BonCommandeLigneJpaEntity;
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
public class AjouterLigneBonCommandeUseCase {

    private final BonCommandeJpaRepository bons;
    private final BonCommandeLigneJpaRepository lignes;
    private final AuditWriter auditWriter;

    public AjouterLigneBonCommandeUseCase(BonCommandeJpaRepository bons, BonCommandeLigneJpaRepository lignes, AuditWriter auditWriter) {
        this.bons = Objects.requireNonNull(bons);
        this.lignes = Objects.requireNonNull(lignes);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public UUID execute(AjouterLigneBonCommandeCommand cmd) {
        Objects.requireNonNull(cmd);
        BonCommandeJpaEntity bc = bons.findByOrganisationIdAndId(cmd.organisationId(), cmd.bonCommandeId())
                .orElseThrow(() -> new BusinessRuleViolationException("Bon de commande introuvable"));
        if (!"BROUILLON".equals(bc.getStatut())) {
            throw new BusinessRuleViolationException("Ajout de ligne interdit sur ce statut");
        }
        lignes.findByBonCommandeIdAndProduitId(cmd.bonCommandeId(), cmd.produitId()).ifPresent(l -> {
            throw new BusinessRuleViolationException("Ligne déjà existante pour ce produit");
        });

        String devise = (cmd.devise() == null || cmd.devise().isBlank()) ? "XAF" : cmd.devise().trim();
        UUID id = UUID.randomUUID();
        lignes.save(BonCommandeLigneJpaEntity.create(
                id,
                cmd.bonCommandeId(),
                cmd.organisationId(),
                cmd.produitId(),
                cmd.quantiteCommandee(),
                cmd.prixAttenduUnitaire(),
                devise
        ));

        Instant now = Instant.now();
        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, null, null, null,
                null, null, "BON_COMMANDE_LIGNE_AJOUTEE", "BonCommande", cmd.bonCommandeId().toString(), null,
                Map.of("produit_id", cmd.produitId(), "quantite_commandee", cmd.quantiteCommandee())
        ));

        return id;
    }
}

