package cm.pharma.contexts.achats_fournisseurs.application.command;

import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.BonCommandeJpaEntity;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.BonCommandeJpaRepository;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.FournisseurJpaEntity;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.FournisseurJpaRepository;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.referentiel.application.service.NumerotationService;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreerBonCommandeUseCase {

    private final FournisseurJpaRepository fournisseurs;
    private final BonCommandeJpaRepository bons;
    private final NumerotationService numerotation;
    private final AuditWriter auditWriter;

    public CreerBonCommandeUseCase(
            FournisseurJpaRepository fournisseurs,
            BonCommandeJpaRepository bons,
            NumerotationService numerotation,
            AuditWriter auditWriter
    ) {
        this.fournisseurs = Objects.requireNonNull(fournisseurs);
        this.bons = Objects.requireNonNull(bons);
        this.numerotation = Objects.requireNonNull(numerotation);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public UUID execute(CreerBonCommandeCommand cmd) {
        Objects.requireNonNull(cmd);
        FournisseurJpaEntity fournisseur = fournisseurs.findByOrganisationIdAndId(cmd.organisationId(), cmd.fournisseurId())
                .orElseThrow(() -> new BusinessRuleViolationException("Fournisseur introuvable"));
        if (!fournisseur.isActif()) {
            throw new BusinessRuleViolationException("Fournisseur inactif");
        }

        Instant now = Instant.now();
        String numero = numerotation.nextNumero(cmd.organisationId(), "BON_COMMANDE");
        UUID id = UUID.randomUUID();

        bons.save(BonCommandeJpaEntity.create(new BonCommandeJpaEntity.BonCommandeInit(
                id,
                cmd.organisationId(),
                numero,
                cmd.fournisseurId(),
                cmd.dateCommande() == null ? LocalDate.now() : cmd.dateCommande(),
                cmd.dateLivraisonPrevue(),
                cmd.commentaire(),
                cmd.creePar(),
                now
        )));

        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, null, null, null,
                null, null, "BON_COMMANDE_CREE", "BonCommande", id.toString(), null,
                Map.of("numero", numero, "fournisseur_id", cmd.fournisseurId())
        ));

        return id;
    }
}

