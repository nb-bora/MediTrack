package cm.pharma.contexts.catalogue_produits.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.catalogue_produits.domain.model.TypePrix;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.PrixProduitJpaEntity;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.PrixProduitJpaRepository;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaEntity;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreerPrixUseCase {

    private final ProduitJpaRepository produits;
    private final PrixProduitJpaRepository prix;
    private final AuditWriter auditWriter;

    public CreerPrixUseCase(ProduitJpaRepository produits, PrixProduitJpaRepository prix, AuditWriter auditWriter) {
        this.produits = Objects.requireNonNull(produits);
        this.prix = Objects.requireNonNull(prix);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    /**
     * Règle : un prix ne se modifie jamais.
     * On crée une nouvelle ligne avec {@code dateDebut} et on clôture automatiquement l’ancien prix actif
     * en positionnant {@code dateFin = dateDebut - 1}.
     */
    @Transactional
    public UUID execute(CreerPrixCommand cmd) {
        Objects.requireNonNull(cmd);
        TypePrix.valueOf(cmd.typePrix()); // validation enum

        ProduitJpaEntity produit = produits.findById(cmd.produitId())
                .orElseThrow(() -> new BusinessRuleViolationException("Produit introuvable"));
        if (!produit.getOrganisationId().equals(cmd.organisationId())) {
            throw new BusinessRuleViolationException("Produit hors organisation");
        }

        if (cmd.dateDebut().isAfter(LocalDate.now().plusYears(50))) {
            throw new BusinessRuleViolationException("Date d'effet invalide");
        }

        // Interdire chevauchement : si un prix est applicable à dateDebut, alors on doit être en train de clôturer l’actif.
        var applicable = prix.findApplicableAt(cmd.produitId(), cmd.typePrix(), cmd.dateDebut());
        if (!applicable.isEmpty()) {
            PrixProduitJpaEntity current = applicable.getFirst();
            if (current.getDateFin() != null && !current.getDateFin().isBefore(cmd.dateDebut())) {
                throw new BusinessRuleViolationException("Chevauchement de prix interdit");
            }
            if (current.getDateFin() == null) {
                if (!cmd.dateDebut().isAfter(current.getDateDebut())) {
                    throw new BusinessRuleViolationException("La date d'effet doit être postérieure au prix actuel");
                }
                current.closeAt(cmd.dateDebut().minusDays(1));
            }
        } else {
            // S’il y a un actif à dateFin=null mais dont dateDebut > dateDebut, c’est un prix futur: on interdit (version simple)
            var actives = prix.findActive(cmd.produitId(), cmd.typePrix());
            if (!actives.isEmpty() && cmd.dateDebut().isBefore(actives.getFirst().getDateDebut())) {
                throw new BusinessRuleViolationException("Impossible d'insérer un prix avant un prix futur déjà défini");
            }
        }

        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        prix.save(PrixProduitJpaEntity.create(new PrixProduitJpaEntity.PrixInit(
                id,
                cmd.produitId(),
                cmd.typePrix(),
                cmd.montant(),
                cmd.devise(),
                cmd.dateDebut(),
                null,
                cmd.motif(),
                cmd.creePar(),
                now
        )));

        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, null, null, null,
                null, null, "PRIX_CREE", "PrixProduit", id.toString(), null,
                Map.of(
                        "produit_id", cmd.produitId(),
                        "type_prix", cmd.typePrix(),
                        "montant", cmd.montant(),
                        "devise", cmd.devise(),
                        "date_debut", cmd.dateDebut(),
                        "motif", cmd.motif()
                )
        ));
        return id;
    }
}

