package cm.pharma.contexts.stocks_tracabilite.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.InventaireJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.InventaireJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.InventaireLigneJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.InventaireLigneJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaisirInventaireLigneUseCase {

    private final InventaireJpaRepository inventaires;
    private final InventaireLigneJpaRepository lignes;
    private final AuditWriter auditWriter;

    public SaisirInventaireLigneUseCase(InventaireJpaRepository inventaires, InventaireLigneJpaRepository lignes, AuditWriter auditWriter) {
        this.inventaires = Objects.requireNonNull(inventaires);
        this.lignes = Objects.requireNonNull(lignes);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public void execute(SaisirInventaireLigneCommand cmd) {
        Objects.requireNonNull(cmd);
        InventaireJpaEntity inv = inventaires.findById(cmd.inventaireId())
                .orElseThrow(() -> new BusinessRuleViolationException("Inventaire introuvable"));
        if (!inv.getOrganisationId().equals(cmd.organisationId())) {
            throw new BusinessRuleViolationException("Inventaire hors organisation");
        }
        if (!"OUVERT".equals(inv.getStatut())) {
            throw new BusinessRuleViolationException("Inventaire non modifiable");
        }

        InventaireLigneJpaEntity ligne = lignes.findByInventaireIdAndProduitIdAndEmplacementId(cmd.inventaireId(), cmd.produitId(), cmd.emplacementId())
                .orElseThrow(() -> new BusinessRuleViolationException("Ligne d’inventaire introuvable"));

        Instant now = Instant.now();
        ligne.saisirStockReel(cmd.stockReel(), cmd.motifEcart(), now);

        if (ligne.getEcart() != null && ligne.getEcart() != 0 && (cmd.motifEcart() == null || cmd.motifEcart().isBlank())) {
            throw new BusinessRuleViolationException("Motif obligatoire en cas d’écart");
        }

        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, null, null, null,
                null, null, "INVENTAIRE_LIGNE_SAISIE", "Inventaire", cmd.inventaireId().toString(), cmd.motifEcart(),
                Map.of(
                        "produit_id", cmd.produitId(),
                        "emplacement_id", cmd.emplacementId(),
                        "stock_reel", cmd.stockReel(),
                        "ecart", ligne.getEcart()
                )
        ));
    }
}

