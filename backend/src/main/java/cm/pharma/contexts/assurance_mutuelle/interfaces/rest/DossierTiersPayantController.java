package cm.pharma.contexts.assurance_mutuelle.interfaces.rest;

import cm.pharma.contexts.assurance_mutuelle.application.command.AjouterPieceDossierTiersPayantUseCase;
import cm.pharma.contexts.assurance_mutuelle.application.command.CreerDossierTiersPayantDepuisVenteUseCase;
import cm.pharma.contexts.assurance_mutuelle.application.command.MarquerDossierTiersPayantPayeUseCase;
import cm.pharma.contexts.assurance_mutuelle.application.command.RejeterDossierTiersPayantUseCase;
import cm.pharma.contexts.assurance_mutuelle.application.command.ResoumettreDossierTiersPayantUseCase;
import cm.pharma.contexts.assurance_mutuelle.application.command.SoumettreDossierTiersPayantUseCase;
import cm.pharma.contexts.assurance_mutuelle.application.service.DossierPieceStorageService;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.DossierTiersPayantJpaRepository;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.DossierTiersPayantPieceJpaRepository;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import cm.pharma.shared.interfaces.rest.PosteContext;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/assurances/dossiers")
public class DossierTiersPayantController {

    private final DossierTiersPayantJpaRepository dossiers;
    private final DossierTiersPayantPieceJpaRepository pieces;
    private final CreerDossierTiersPayantDepuisVenteUseCase creerDepuisVente;
    private final SoumettreDossierTiersPayantUseCase soumettre;
    private final RejeterDossierTiersPayantUseCase rejeter;
    private final ResoumettreDossierTiersPayantUseCase resoumettre;
    private final MarquerDossierTiersPayantPayeUseCase payer;
    private final AjouterPieceDossierTiersPayantUseCase ajouterPiece;
    private final DossierPieceStorageService storage;

    public DossierTiersPayantController(
            DossierTiersPayantJpaRepository dossiers,
            DossierTiersPayantPieceJpaRepository pieces,
            CreerDossierTiersPayantDepuisVenteUseCase creerDepuisVente,
            SoumettreDossierTiersPayantUseCase soumettre,
            RejeterDossierTiersPayantUseCase rejeter,
            ResoumettreDossierTiersPayantUseCase resoumettre,
            MarquerDossierTiersPayantPayeUseCase payer,
            AjouterPieceDossierTiersPayantUseCase ajouterPiece,
            DossierPieceStorageService storage
    ) {
        this.dossiers = dossiers;
        this.pieces = pieces;
        this.creerDepuisVente = creerDepuisVente;
        this.soumettre = soumettre;
        this.rejeter = rejeter;
        this.resoumettre = resoumettre;
        this.payer = payer;
        this.ajouterPiece = ajouterPiece;
        this.storage = storage;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public CreerDossierResponse creer(@Valid @RequestBody CreerDossierRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID actorId = UUID.fromString(auth.getToken().getSubject());
        UUID dossierId = creerDepuisVente.execute(orgId, req.venteId(), actorId);
        return new CreerDossierResponse(dossierId);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE','PHARMACIEN')")
    public List<DossierItem> lister(@RequestParam(required = false) String statut, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        if (statut == null || statut.isBlank()) {
            // défaut: dossiers récents en attente
            return dossiers.findByOrganisationIdAndStatutOrderByCreatedAtDesc(orgId, "SOUMIS").stream()
                    .map(DossierItem::from)
                    .toList();
        }
        return dossiers.findByOrganisationIdAndStatutOrderByCreatedAtDesc(orgId, statut.toUpperCase()).stream()
                .map(DossierItem::from)
                .toList();
    }

    @GetMapping("/{dossierId}/pieces")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE','PHARMACIEN')")
    public List<PieceItem> listerPieces(@PathVariable UUID dossierId, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        return pieces.findByOrganisationIdAndDossierId(orgId, dossierId).stream()
                .map(p -> new PieceItem(p.getId(), p.getTypePiece(), p.getFichierNom(), p.getContenuType(), p.getCreatedAt()))
                .toList();
    }

    @PostMapping("/{dossierId}/pieces")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public PieceUploadResponse uploadPiece(
            @PathVariable UUID dossierId,
            @RequestParam("type_piece") @NotBlank String typePiece,
            @RequestParam("file") @NotNull MultipartFile file,
            JwtAuthenticationToken auth
    ) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID actorId = UUID.fromString(auth.getToken().getSubject());
        PosteContext.posteNom(auth); // force claim presence (audit elsewhere)
        UUID pieceId = ajouterPiece.execute(orgId, dossierId, typePiece, file, actorId);
        return new PieceUploadResponse(pieceId);
    }

    @GetMapping("/{dossierId}/pieces/{pieceId}/download")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE','PHARMACIEN')")
    public ResponseEntity<Resource> download(@PathVariable UUID dossierId, @PathVariable UUID pieceId, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        var p = pieces.findByOrganisationIdAndId(orgId, pieceId)
                .orElseThrow(() -> new BusinessRuleViolationException("Pièce introuvable"));
        if (!p.getDossierId().equals(dossierId)) {
            return ResponseEntity.notFound().build();
        }
        Resource r = storage.loadAsResource(p.getStorageKey());
        MediaType mt = p.getContenuType() == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(p.getContenuType());
        return ResponseEntity.ok()
                .contentType(mt)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + p.getFichierNom() + "\"")
                .body(r);
    }

    @PostMapping("/{dossierId}/soumission")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public void soumettre(@PathVariable UUID dossierId, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID actorId = UUID.fromString(auth.getToken().getSubject());
        soumettre.execute(orgId, dossierId, actorId);
    }

    @PostMapping("/{dossierId}/rejet")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public void rejeter(@PathVariable UUID dossierId, @Valid @RequestBody RejetRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID actorId = UUID.fromString(auth.getToken().getSubject());
        rejeter.execute(orgId, dossierId, req.motif(), actorId);
    }

    @PostMapping("/{dossierId}/resoumission")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public void resoumettre(@PathVariable UUID dossierId, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID actorId = UUID.fromString(auth.getToken().getSubject());
        resoumettre.execute(orgId, dossierId, actorId);
    }

    @PostMapping("/{dossierId}/paiement")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public void payer(@PathVariable UUID dossierId, @RequestBody(required = false) PaiementRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID actorId = UUID.fromString(auth.getToken().getSubject());
        payer.execute(orgId, dossierId, req == null ? null : req.reference(), actorId);
    }

    @GetMapping("/stats/rejets")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public List<MotifRejetStat> statsRejets(JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        return dossiers.statsMotifsRejet(orgId).stream()
                .map(r -> new MotifRejetStat(r.getMotif(), r.getNb()))
                .toList();
    }

    public record DossierItem(UUID id, String numeroDossier, String statut, UUID organismeId, UUID patientId, UUID venteId) {
        static DossierItem from(cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.DossierTiersPayantJpaEntity d) {
            return new DossierItem(d.getId(), d.getNumeroDossier(), d.getStatut(), d.getOrganismeId(), d.getPatientId(), d.getVenteId());
        }
    }

    public record PieceItem(UUID id, String typePiece, String fichierNom, String contenuType, java.time.Instant createdAt) {
    }

    public record PieceUploadResponse(UUID pieceId) {
    }

    public record CreerDossierRequest(@NotNull UUID venteId) {
    }

    public record CreerDossierResponse(UUID dossierId) {
    }

    public record RejetRequest(@NotBlank String motif) {
    }

    public record MotifRejetStat(String motif, long nb) {
    }

    public record PaiementRequest(String reference) {
    }
}

