package cm.pharma.contexts.catalogue_produits.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.CodeBarresProduitJpaEntity;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.CodeBarresProduitJpaRepository;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaEntity;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaRepository;
import cm.pharma.contexts.referentiel.application.service.ParametresService;
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
    private final ParametresService parametres;
    private final AuditWriter auditWriter;

    public AjouterCodeBarresUseCase(ProduitJpaRepository produits, CodeBarresProduitJpaRepository codesBarres, ParametresService parametres, AuditWriter auditWriter) {
        this.produits = Objects.requireNonNull(produits);
        this.codesBarres = Objects.requireNonNull(codesBarres);
        this.parametres = Objects.requireNonNull(parametres);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public UUID execute(AjouterCodeBarresCommand cmd) {
        Objects.requireNonNull(cmd, "cmd requis");

        ProduitJpaEntity produit = produits.findById(cmd.produitId())
                .orElseThrow(() -> new BusinessRuleViolationException("Produit introuvable"));
        if (!produit.getOrganisationId().equals(cmd.organisationId())) {
            throw new BusinessRuleViolationException("Produit hors organisation");
        }
        String ean = cmd.ean13() == null ? null : cmd.ean13().trim();
        if (ean == null || !ean.matches("^\\d{13}$")) {
            throw new BusinessRuleViolationException("EAN13 doit contenir 13 chiffres");
        }
        boolean uniqueGlobal = parametres.getBoolean(cmd.organisationId(), "EAN13_UNIQUE_GLOBAL", true);
        boolean exists = uniqueGlobal ? codesBarres.existsByEan13(ean) : codesBarres.existsByOrganisationIdAndEan13(cmd.organisationId(), ean);
        if (exists) {
            throw new BusinessRuleViolationException("EAN13 déjà utilisé par un autre produit");
        }

        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        codesBarres.save(CodeBarresProduitJpaEntity.create(id, cmd.produitId(), ean, cmd.libelle(), now));

        auditWriter.write(AuditEvent.simple(
                produit.getOrganisationId(),
                now, null, null, null,
                null, null, "CODE_BARRES_CREE", "CodeBarresProduit", id.toString(), null,
                Map.of("produit_id", cmd.produitId().toString(), "ean13", ean)
        ));
        return id;
    }
}

