package cm.pharma.contexts.assurance_mutuelle.application.command;

import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.DossierTiersPayantJpaEntity;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.DossierTiersPayantJpaRepository;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.DossierTiersPayantPieceJpaRepository;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.OrganismeCouvertureJpaEntity;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.OrganismeCouvertureJpaRepository;
import cm.pharma.shared.application.AlerteService;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SoumettreDossierTiersPayantUseCase {

    private final DossierTiersPayantJpaRepository dossiers;
    private final OrganismeCouvertureJpaRepository couvertures;
    private final DossierTiersPayantPieceJpaRepository pieces;
    private final AlerteService alertes;

    public SoumettreDossierTiersPayantUseCase(
            DossierTiersPayantJpaRepository dossiers,
            OrganismeCouvertureJpaRepository couvertures,
            DossierTiersPayantPieceJpaRepository pieces,
            AlerteService alertes
    ) {
        this.dossiers = Objects.requireNonNull(dossiers);
        this.couvertures = Objects.requireNonNull(couvertures);
        this.pieces = Objects.requireNonNull(pieces);
        this.alertes = Objects.requireNonNull(alertes);
    }

    @Transactional
    public void execute(UUID organisationId, UUID dossierId, UUID actorId) {
        DossierTiersPayantJpaEntity d = dossiers.findByOrganisationIdAndId(organisationId, dossierId)
                .orElseThrow(() -> new BusinessRuleViolationException("Dossier introuvable"));
        if (!"BROUILLON".equals(d.getStatut())) {
            throw new BusinessRuleViolationException("Dossier non soumettable");
        }
        OrganismeCouvertureJpaEntity c = couvertures.findByOrganisationIdAndOrganismeId(organisationId, d.getOrganismeId())
                .orElseThrow(() -> new BusinessRuleViolationException("Couverture organisme non définie"));

        verifierPiecesObligatoires(organisationId, dossierId, c);

        d.soumettre(actorId, Instant.now());
        dossiers.save(d);

        alertes.resolveDedup(organisationId, "DOSSIER_TP_PIECES_A_VERIFIER", "DossierTiersPayant", dossierId.toString(), actorId, "Soumis");
        alertes.openDedup(organisationId, "DOSSIER_TP_SOUMIS", "INFO", "DossierTiersPayant", dossierId.toString(), "Dossier soumis", actorId);
    }

    private void verifierPiecesObligatoires(UUID organisationId, UUID dossierId, OrganismeCouvertureJpaEntity c) {
        if (c.isPieceOrdonnanceOriginale() && !pieces.existsByOrganisationIdAndDossierIdAndTypePieceIgnoreCase(organisationId, dossierId, "ORDONNANCE_ORIGINALE")) {
            throw new BusinessRuleViolationException("Pièce manquante: ORDONNANCE_ORIGINALE");
        }
        if (c.isPieceCarteAdherent() && !pieces.existsByOrganisationIdAndDossierIdAndTypePieceIgnoreCase(organisationId, dossierId, "CARTE_ADHERENT")) {
            throw new BusinessRuleViolationException("Pièce manquante: CARTE_ADHERENT");
        }
        if (c.isPieceBonPriseEnCharge() && !pieces.existsByOrganisationIdAndDossierIdAndTypePieceIgnoreCase(organisationId, dossierId, "BON_PRISE_EN_CHARGE")) {
            throw new BusinessRuleViolationException("Pièce manquante: BON_PRISE_EN_CHARGE");
        }
        if (c.isPieceExamens() && !pieces.existsByOrganisationIdAndDossierIdAndTypePieceIgnoreCase(organisationId, dossierId, "EXAMENS")) {
            throw new BusinessRuleViolationException("Pièce manquante: EXAMENS");
        }
    }
}

