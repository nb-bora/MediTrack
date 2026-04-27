package cm.pharma.contexts.ordonnances_dossier_patient.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.ordonnances_dossier_patient.application.service.OrdonnancePieceStorageService;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceJpaEntity;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceJpaRepository;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnancePieceJpaEntity;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnancePieceJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AjouterPieceOrdonnanceUseCase {

    private final OrdonnanceJpaRepository ordonnances;
    private final OrdonnancePieceJpaRepository pieces;
    private final OrdonnancePieceStorageService storage;
    private final AuditWriter auditWriter;

    public AjouterPieceOrdonnanceUseCase(
            OrdonnanceJpaRepository ordonnances,
            OrdonnancePieceJpaRepository pieces,
            OrdonnancePieceStorageService storage,
            AuditWriter auditWriter
    ) {
        this.ordonnances = Objects.requireNonNull(ordonnances);
        this.pieces = Objects.requireNonNull(pieces);
        this.storage = Objects.requireNonNull(storage);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public UUID execute(UUID organisationId, UUID ordonnanceId, MultipartFile file, UUID actorId, String posteNom) {
        OrdonnanceJpaEntity o = ordonnances.findByOrganisationIdAndId(organisationId, ordonnanceId)
                .orElseThrow(() -> new BusinessRuleViolationException("Ordonnance introuvable"));

        OrdonnancePieceStorageService.StoredPiece stored = storage.store(organisationId, ordonnanceId, file);
        Instant now = Instant.now();
        pieces.save(OrdonnancePieceJpaEntity.create(
                stored.pieceId(),
                organisationId,
                ordonnanceId,
                stored.fichierNom(),
                stored.contenuType(),
                stored.storageKey(),
                now
        ));

        auditWriter.write(AuditEvent.simple(
                organisationId, now, actorId, null, null,
                posteNom, null, "ORDONNANCE_PIECE_AJOUTEE", "Ordonnance", o.getId().toString(), null,
                Map.of("piece_id", stored.pieceId(), "storage_key", stored.storageKey())
        ));
        return stored.pieceId();
    }
}

