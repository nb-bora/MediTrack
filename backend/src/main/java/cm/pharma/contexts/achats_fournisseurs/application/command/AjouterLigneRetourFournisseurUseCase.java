package cm.pharma.contexts.achats_fournisseurs.application.command;

import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.RetourFournisseurJpaEntity;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.RetourFournisseurJpaRepository;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.RetourFournisseurLigneJpaEntity;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.RetourFournisseurLigneJpaRepository;
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
public class AjouterLigneRetourFournisseurUseCase {

    private final RetourFournisseurJpaRepository retours;
    private final RetourFournisseurLigneJpaRepository lignes;
    private final AuditWriter auditWriter;

    public AjouterLigneRetourFournisseurUseCase(RetourFournisseurJpaRepository retours, RetourFournisseurLigneJpaRepository lignes, AuditWriter auditWriter) {
        this.retours = Objects.requireNonNull(retours);
        this.lignes = Objects.requireNonNull(lignes);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public UUID execute(UUID organisationId, UUID retourId, UUID lotId, int quantite, String motif, UUID actorId) {
        RetourFournisseurJpaEntity retour = retours.findByOrganisationIdAndId(organisationId, retourId)
                .orElseThrow(() -> new BusinessRuleViolationException("Retour introuvable"));
        if (!"BROUILLON".equals(retour.getStatut())) {
            throw new BusinessRuleViolationException("Ajout de ligne interdit sur ce statut");
        }
        if (quantite <= 0) {
            throw new BusinessRuleViolationException("Quantité invalide");
        }
        UUID id = UUID.randomUUID();
        lignes.save(RetourFournisseurLigneJpaEntity.create(id, retourId, organisationId, lotId, quantite, motif));

        Instant now = Instant.now();
        auditWriter.write(AuditEvent.simple(
                organisationId, now, null, null, null,
                null, null, "RETOUR_FOURNISSEUR_LIGNE_AJOUTEE", "RetourFournisseur", retourId.toString(), motif,
                Map.of("lot_id", lotId, "quantite", quantite)
        ));
        return id;
    }
}

