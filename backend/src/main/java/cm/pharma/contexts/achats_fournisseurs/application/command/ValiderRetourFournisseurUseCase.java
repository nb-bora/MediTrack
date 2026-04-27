package cm.pharma.contexts.achats_fournisseurs.application.command;

import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.RetourFournisseurJpaEntity;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.RetourFournisseurJpaRepository;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.RetourFournisseurLigneJpaRepository;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.stocks_tracabilite.application.command.SortirLotPourRetourFournisseurUseCase;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValiderRetourFournisseurUseCase {

    private final RetourFournisseurJpaRepository retours;
    private final RetourFournisseurLigneJpaRepository lignes;
    private final SortirLotPourRetourFournisseurUseCase sortirLot;
    private final AuditWriter auditWriter;

    public ValiderRetourFournisseurUseCase(
            RetourFournisseurJpaRepository retours,
            RetourFournisseurLigneJpaRepository lignes,
            SortirLotPourRetourFournisseurUseCase sortirLot,
            AuditWriter auditWriter
    ) {
        this.retours = Objects.requireNonNull(retours);
        this.lignes = Objects.requireNonNull(lignes);
        this.sortirLot = Objects.requireNonNull(sortirLot);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public void execute(UUID organisationId, UUID retourId, UUID actorId) {
        RetourFournisseurJpaEntity retour = retours.findByOrganisationIdAndId(organisationId, retourId)
                .orElseThrow(() -> new BusinessRuleViolationException("Retour introuvable"));
        if (!"BROUILLON".equals(retour.getStatut())) {
            throw new BusinessRuleViolationException("Retour non validable");
        }
        var lignesRetour = lignes.findByRetourId(retourId);
        if (lignesRetour.isEmpty()) {
            throw new BusinessRuleViolationException("Retour sans lignes");
        }

        for (var l : lignesRetour) {
            sortirLot.execute(organisationId, l.getLotId(), l.getQuantite(), retour.getMotif(), actorId, "RET-" + retourId);
        }

        Instant now = Instant.now();
        retour.valider(actorId, now);

        auditWriter.write(AuditEvent.simple(
                organisationId, now, null, null, null,
                null, null, "RETOUR_FOURNISSEUR_VALIDE", "RetourFournisseur", retourId.toString(), retour.getMotif(),
                Map.of()
        ));
    }
}

