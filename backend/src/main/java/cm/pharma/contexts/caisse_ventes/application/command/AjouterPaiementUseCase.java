package cm.pharma.contexts.caisse_ventes.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.PaiementVenteJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.PaiementVenteJpaRepository;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AjouterPaiementUseCase {

    private static final Set<String> MODES = Set.of(
            "ESPECES",
            "MOBILE_MONEY_MTN",
            "MOBILE_MONEY_ORANGE",
            "VIREMENT",
            "CHEQUE",
            "TIERS_PAYANT",
            "AVOIR"
    );

    private final VenteJpaRepository ventes;
    private final PaiementVenteJpaRepository paiements;
    private final AuditWriter auditWriter;

    public AjouterPaiementUseCase(VenteJpaRepository ventes, PaiementVenteJpaRepository paiements, AuditWriter auditWriter) {
        this.ventes = Objects.requireNonNull(ventes);
        this.paiements = Objects.requireNonNull(paiements);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public UUID execute(AjouterPaiementCommand cmd) {
        Objects.requireNonNull(cmd);
        if (!MODES.contains(cmd.modePaiement())) {
            throw new BusinessRuleViolationException("Mode de paiement non supporté");
        }
        VenteJpaEntity vente = ventes.findByOrganisationIdAndId(cmd.organisationId(), cmd.venteId())
                .orElseThrow(() -> new BusinessRuleViolationException("Vente introuvable"));
        if (!"BROUILLON".equals(vente.getStatut())) {
            throw new BusinessRuleViolationException("Paiement interdit sur ce statut");
        }
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        paiements.save(PaiementVenteJpaEntity.create(id, cmd.venteId(), cmd.organisationId(), cmd.modePaiement(), cmd.montant(), cmd.reference(), now));

        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, cmd.creePar(), null, null,
                cmd.posteNom(), null, "PAIEMENT_AJOUTE", "Vente", vente.getNumeroVente(), null,
                Map.of("mode", cmd.modePaiement(), "montant", cmd.montant())
        ));
        return id;
    }
}

