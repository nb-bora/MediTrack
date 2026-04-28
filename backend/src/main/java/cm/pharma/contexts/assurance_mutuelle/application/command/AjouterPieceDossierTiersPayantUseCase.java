package cm.pharma.contexts.assurance_mutuelle.application.command;

import cm.pharma.contexts.assurance_mutuelle.application.service.DossierPieceStorageService;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.DossierTiersPayantJpaEntity;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.DossierTiersPayantJpaRepository;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.DossierTiersPayantPieceJpaEntity;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.DossierTiersPayantPieceJpaRepository;
import cm.pharma.shared.application.AlerteService;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AjouterPieceDossierTiersPayantUseCase {

    private final DossierTiersPayantJpaRepository dossiers;
    private final DossierTiersPayantPieceJpaRepository pieces;
    private final DossierPieceStorageService storage;
    private final AlerteService alertes;

    public AjouterPieceDossierTiersPayantUseCase(
            DossierTiersPayantJpaRepository dossiers,
            DossierTiersPayantPieceJpaRepository pieces,
            DossierPieceStorageService storage,
            AlerteService alertes
    ) {
        this.dossiers = Objects.requireNonNull(dossiers);
        this.pieces = Objects.requireNonNull(pieces);
        this.storage = Objects.requireNonNull(storage);
        this.alertes = Objects.requireNonNull(alertes);
    }

    @Transactional
    public UUID execute(UUID organisationId, UUID dossierId, String typePiece, MultipartFile file, UUID actorId) {
        DossierTiersPayantJpaEntity d = dossiers.findByOrganisationIdAndId(organisationId, dossierId)
                .orElseThrow(() -> new BusinessRuleViolationException("Dossier introuvable"));
        if (!"BROUILLON".equals(d.getStatut()) && !"REJETE".equals(d.getStatut())) {
            throw new BusinessRuleViolationException("Pièces non modifiables à ce statut");
        }
        if (typePiece == null || typePiece.isBlank()) {
            throw new BusinessRuleViolationException("type_piece requis");
        }

        var stored = storage.store(organisationId, dossierId, file);
        Instant now = Instant.now();
        pieces.save(DossierTiersPayantPieceJpaEntity.create(
                stored.pieceId(),
                organisationId,
                dossierId,
                typePiece.toUpperCase(),
                stored.fichierNom(),
                stored.contenuType(),
                stored.storageKey(),
                actorId,
                now
        ));

        // On laisse l’alerte “pièces à vérifier” ouverte tant que le dossier n’est pas soumis;
        // elle sera résolue automatiquement à la soumission si tout est conforme.
        alertes.openDedup(
                organisationId,
                "DOSSIER_TP_PIECE_AJOUTEE",
                "INFO",
                "DossierTiersPayant",
                dossierId.toString(),
                "Pièce ajoutée: " + typePiece,
                actorId
        );

        return stored.pieceId();
    }
}

