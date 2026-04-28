package cm.pharma.contexts.assurance_mutuelle.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.OrganismeAssuranceJpaRepository;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.OrganismeCouvertureJpaEntity;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.OrganismeCouvertureJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefinirCouvertureOrganismeUseCase {

    private final OrganismeAssuranceJpaRepository organismes;
    private final OrganismeCouvertureJpaRepository couvertures;
    private final AuditWriter auditWriter;

    public DefinirCouvertureOrganismeUseCase(OrganismeAssuranceJpaRepository organismes, OrganismeCouvertureJpaRepository couvertures, AuditWriter auditWriter) {
        this.organismes = Objects.requireNonNull(organismes);
        this.couvertures = Objects.requireNonNull(couvertures);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public void execute(DefinirCouvertureCommand cmd) {
        Objects.requireNonNull(cmd);
        organismes.findByOrganisationIdAndId(cmd.organisationId(), cmd.organismeId())
                .orElseThrow(() -> new BusinessRuleViolationException("Organisme introuvable"));

        validatePct(cmd.tauxGenerique());
        validatePct(cmd.tauxMarque());
        validatePct(cmd.tauxParapharma());
        validatePct(cmd.tauxStupefiants());

        OrganismeCouvertureJpaEntity existing = couvertures.findByOrganisationIdAndOrganismeId(cmd.organisationId(), cmd.organismeId()).orElse(null);
        OrganismeCouvertureJpaEntity c = OrganismeCouvertureJpaEntity.createOrUpdate(existing, new OrganismeCouvertureJpaEntity.CouvertureInit(
                existing == null ? UUID.randomUUID() : existing.getId(),
                cmd.organismeId(),
                cmd.organisationId(),
                cmd.tauxGenerique(),
                cmd.tauxMarque(),
                cmd.tauxParapharma(),
                cmd.tauxStupefiants(),
                cmd.plafondJournalier(),
                cmd.plafondMensuel(),
                cmd.plafondAnnuel(),
                cmd.pieceOrdonnanceOriginale(),
                cmd.pieceCarteAdherent(),
                cmd.pieceBonPriseEnCharge(),
                cmd.pieceExamens()
        ));
        couvertures.save(c);

        Instant now = Instant.now();
        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, cmd.actorId(), null, null,
                cmd.posteNom(), null, "ORGANISME_COUVERTURE_MAJ", "OrganismeAssurance", cmd.organismeId().toString(), null,
                Map.of("taux_generique", cmd.tauxGenerique(), "taux_marque", cmd.tauxMarque())
        ));
    }

    private static void validatePct(BigDecimal pct) {
        if (pct == null || pct.compareTo(BigDecimal.ZERO) < 0 || pct.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessRuleViolationException("Taux invalide");
        }
    }

    public record DefinirCouvertureCommand(
            UUID organisationId,
            UUID organismeId,
            BigDecimal tauxGenerique,
            BigDecimal tauxMarque,
            BigDecimal tauxParapharma,
            BigDecimal tauxStupefiants,
            BigDecimal plafondJournalier,
            BigDecimal plafondMensuel,
            BigDecimal plafondAnnuel,
            boolean pieceOrdonnanceOriginale,
            boolean pieceCarteAdherent,
            boolean pieceBonPriseEnCharge,
            boolean pieceExamens,
            UUID actorId,
            String posteNom
    ) {
    }
}

