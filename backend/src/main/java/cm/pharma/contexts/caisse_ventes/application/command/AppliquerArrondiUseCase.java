package cm.pharma.contexts.caisse_ventes.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteJpaRepository;
import cm.pharma.contexts.referentiel.application.service.ParametresService;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppliquerArrondiUseCase {

    private final VenteJpaRepository ventes;
    private final ParametresService parametres;
    private final AuditWriter auditWriter;

    public AppliquerArrondiUseCase(VenteJpaRepository ventes, ParametresService parametres, AuditWriter auditWriter) {
        this.ventes = Objects.requireNonNull(ventes);
        this.parametres = Objects.requireNonNull(parametres);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public void execute(UUID organisationId, UUID venteId, BigDecimal montant, UUID actorId, String posteNom, boolean isCaissier) {
        if (montant == null || montant.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("Arrondi invalide");
        }
        BigDecimal maxCaissier = parametres.getBigDecimal(organisationId, "ARRONDI_MAX_CAISSIER", new BigDecimal("50"));
        if (isCaissier && montant.compareTo(maxCaissier) > 0) {
            throw new BusinessRuleViolationException("Arrondi caissier limité à " + maxCaissier + " XAF");
        }

        VenteJpaEntity vente = ventes.findByOrganisationIdAndId(organisationId, venteId)
                .orElseThrow(() -> new BusinessRuleViolationException("Vente introuvable"));
        if (!"BROUILLON".equals(vente.getStatut())) {
            throw new BusinessRuleViolationException("Arrondi interdit sur ce statut");
        }

        Instant now = Instant.now();
        vente.setArrondi(montant);

        auditWriter.write(AuditEvent.simple(
                organisationId, now, actorId, null, null,
                posteNom, null, "VENTE_ARRONDI_APPLIQUE", "Vente", vente.getNumeroVente(), null,
                Map.of("arrondi", montant)
        ));
    }
}

