package cm.pharma.contexts.catalogue_produits.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.CodeBarresProduitJpaEntity;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.CodeBarresProduitJpaRepository;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AjouterCodeBarresUseCase {

    private final ProduitJpaRepository produits;
    private final CodeBarresProduitJpaRepository codesBarres;
    private final AuditWriter auditWriter;

    public AjouterCodeBarresUseCase(ProduitJpaRepository produits, CodeBarresProduitJpaRepository codesBarres, AuditWriter auditWriter) {
        this.produits = Objects.requireNonNull(produits);
        this.codesBarres = Objects.requireNonNull(codesBarres);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public UUID execute(AjouterCodeBarresCommand cmd) {
        Objects.requireNonNull(cmd, "cmd requis");

        if (!produits.existsById(cmd.produitId())) {
            throw new BusinessRuleViolationException("Produit introuvable");
        }
        if (codesBarres.existsByEan13(cmd.ean13())) {
            throw new BusinessRuleViolationException("EAN13 déjà utilisé par un autre produit");
        }

        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        codesBarres.save(CodeBarresProduitJpaEntity.create(id, cmd.produitId(), cmd.ean13(), cmd.libelle(), now));

        auditWriter.write(AuditEvent.simple(
                produits.findById(cmd.produitId()).map(p -> p.getOrganisationId()).orElse(null),
                now, null, null, null,
                null, null, "CODE_BARRES_CREE", "CodeBarresProduit", id.toString(), null,
                Map.of("produit_id", cmd.produitId().toString(), "ean13", cmd.ean13())
        ));
        return id;
    }
}

