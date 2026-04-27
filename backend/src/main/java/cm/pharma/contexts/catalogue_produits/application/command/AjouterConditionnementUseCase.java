package cm.pharma.contexts.catalogue_produits.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ConditionnementProduitJpaEntity;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ConditionnementProduitJpaRepository;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaEntity;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AjouterConditionnementUseCase {

    private final ProduitJpaRepository produits;
    private final ConditionnementProduitJpaRepository conditionnements;
    private final AuditWriter auditWriter;

    public AjouterConditionnementUseCase(
            ProduitJpaRepository produits,
            ConditionnementProduitJpaRepository conditionnements,
            AuditWriter auditWriter
    ) {
        this.produits = Objects.requireNonNull(produits);
        this.conditionnements = Objects.requireNonNull(conditionnements);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public UUID execute(AjouterConditionnementCommand cmd) {
        Objects.requireNonNull(cmd);
        ProduitJpaEntity produit = produits.findById(cmd.produitId())
                .orElseThrow(() -> new BusinessRuleViolationException("Produit introuvable"));
        if (!produit.getOrganisationId().equals(cmd.organisationId())) {
            throw new BusinessRuleViolationException("Produit hors organisation");
        }

        String nom = cmd.nom().trim();
        if (conditionnements.existsByProduitIdAndNomIgnoreCase(cmd.produitId(), nom)) {
            throw new BusinessRuleViolationException("Conditionnement déjà existant pour ce produit");
        }

        Instant now = Instant.now();
        if (cmd.estPrincipal()) {
            for (ConditionnementProduitJpaEntity c : conditionnements.findByProduitId(cmd.produitId())) {
                if (c.isEstPrincipal()) {
                    c.setEstPrincipal(false, now);
                }
            }
        }

        UUID id = UUID.randomUUID();
        ConditionnementProduitJpaEntity created = ConditionnementProduitJpaEntity.create(
                id,
                cmd.produitId(),
                nom,
                cmd.uniteBaseNom().trim(),
                cmd.quantiteUniteBase(),
                cmd.estPrincipal(),
                now
        );
        conditionnements.save(created);

        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, null, null, null,
                null, null, "CONDITIONNEMENT_CREE", "ConditionnementProduit", id.toString(), null,
                Map.of(
                        "produit_id", cmd.produitId(),
                        "nom", nom,
                        "unite_base_nom", cmd.uniteBaseNom(),
                        "quantite_unite_base", cmd.quantiteUniteBase(),
                        "est_principal", cmd.estPrincipal()
                )
        ));
        return id;
    }
}

