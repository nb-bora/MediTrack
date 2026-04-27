package cm.pharma.contexts.catalogue_produits.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.CategorieProduitJpaEntity;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.CategorieProduitJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreerCategorieUseCase {

    private final CategorieProduitJpaRepository categories;
    private final AuditWriter auditWriter;

    public CreerCategorieUseCase(CategorieProduitJpaRepository categories, AuditWriter auditWriter) {
        this.categories = Objects.requireNonNull(categories);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public UUID execute(CreerCategorieCommand cmd) {
        Objects.requireNonNull(cmd);
        if (cmd.nom() == null || cmd.nom().isBlank()) {
            throw new BusinessRuleViolationException("Nom de catégorie requis");
        }
        if (categories.existsByOrganisationIdAndParentIdAndNomIgnoreCase(cmd.organisationId(), cmd.parentId(), cmd.nom().trim())) {
            throw new BusinessRuleViolationException("Catégorie déjà existante");
        }
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        categories.save(CategorieProduitJpaEntity.create(id, cmd.organisationId(), cmd.parentId(), cmd.nom().trim(), now));

        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, null, null, null,
                null, null, "CATEGORIE_CREE", "CategorieProduit", id.toString(), null,
                Map.of("nom", cmd.nom(), "parent_id", cmd.parentId())
        ));
        return id;
    }
}

