package cm.pharma.contexts.catalogue_produits.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaEntity;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaRepository;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitSubstitutId;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitSubstitutJpaEntity;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitSubstitutJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LierSubstitutUseCase {

    private final ProduitJpaRepository produits;
    private final ProduitSubstitutJpaRepository substituts;
    private final AuditWriter auditWriter;

    public LierSubstitutUseCase(ProduitJpaRepository produits, ProduitSubstitutJpaRepository substituts, AuditWriter auditWriter) {
        this.produits = Objects.requireNonNull(produits);
        this.substituts = Objects.requireNonNull(substituts);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public void execute(LierSubstitutCommand cmd) {
        Objects.requireNonNull(cmd);
        if (cmd.produitId().equals(cmd.substitutProduitId())) {
            throw new BusinessRuleViolationException("Un produit ne peut pas être substitut de lui-même");
        }

        ProduitJpaEntity p1 = produits.findById(cmd.produitId())
                .orElseThrow(() -> new BusinessRuleViolationException("Produit introuvable"));
        ProduitJpaEntity p2 = produits.findById(cmd.substitutProduitId())
                .orElseThrow(() -> new BusinessRuleViolationException("Substitut introuvable"));

        if (!p1.getOrganisationId().equals(cmd.organisationId()) || !p2.getOrganisationId().equals(cmd.organisationId())) {
            throw new BusinessRuleViolationException("Produit hors organisation");
        }

        // On enregistre le lien dans les deux sens pour simplifier les usages (recherche et affichage).
        ProduitSubstitutId id12 = new ProduitSubstitutId(cmd.produitId(), cmd.substitutProduitId());
        ProduitSubstitutId id21 = new ProduitSubstitutId(cmd.substitutProduitId(), cmd.produitId());
        if (substituts.existsById(id12) || substituts.existsById(id21)) {
            throw new BusinessRuleViolationException("Lien de substitut déjà existant");
        }

        Instant now = Instant.now();
        substituts.save(ProduitSubstitutJpaEntity.link(id12, cmd.niveau().name(), cmd.creePar(), now));
        substituts.save(ProduitSubstitutJpaEntity.link(id21, cmd.niveau().name(), cmd.creePar(), now));

        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, null, null, null,
                null, null, "SUBSTITUT_LIE", "Produit", cmd.produitId().toString(), null,
                Map.of(
                        "substitut_produit_id", cmd.substitutProduitId(),
                        "niveau", cmd.niveau().name()
                )
        ));
    }
}

