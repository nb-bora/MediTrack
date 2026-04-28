package cm.pharma.contexts.ordonnances_dossier_patient.interfaces.rest;

import cm.pharma.contexts.ordonnances_dossier_patient.application.command.CreerOrdonnanceUseCase;
import cm.pharma.contexts.ordonnances_dossier_patient.application.command.DispenserOrdonnanceUseCase;
import cm.pharma.contexts.ordonnances_dossier_patient.application.command.AjouterPieceOrdonnanceUseCase;
import cm.pharma.contexts.ordonnances_dossier_patient.application.command.ValiderOrdonnanceUseCase;
import cm.pharma.contexts.ordonnances_dossier_patient.application.command.RenouvelerOrdonnanceUseCase;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.DispensationJpaRepository;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceJpaEntity;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceJpaRepository;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnanceLigneJpaRepository;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.OrdonnancePieceJpaRepository;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaRepository;
import cm.pharma.contexts.ordonnances_dossier_patient.application.service.OrdonnancePieceStorageService;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import cm.pharma.shared.interfaces.rest.PosteContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ordonnances")
public class OrdonnanceController {

    private final CreerOrdonnanceUseCase creerOrdonnance;
    private final ValiderOrdonnanceUseCase validerOrdonnance;
    private final DispenserOrdonnanceUseCase dispenser;
    private final AjouterPieceOrdonnanceUseCase ajouterPiece;
    private final OrdonnanceJpaRepository ordonnances;
    private final OrdonnanceLigneJpaRepository lignes;
    private final OrdonnancePieceJpaRepository pieces;
    private final DispensationJpaRepository dispensations;
    private final ProduitJpaRepository produits;
    private final OrdonnancePieceStorageService storage;
    private final RenouvelerOrdonnanceUseCase renouveler;

    public OrdonnanceController(
            CreerOrdonnanceUseCase creerOrdonnance,
            ValiderOrdonnanceUseCase validerOrdonnance,
            DispenserOrdonnanceUseCase dispenser,
            AjouterPieceOrdonnanceUseCase ajouterPiece,
            OrdonnanceJpaRepository ordonnances,
            OrdonnanceLigneJpaRepository lignes,
            OrdonnancePieceJpaRepository pieces,
            DispensationJpaRepository dispensations,
            ProduitJpaRepository produits,
            OrdonnancePieceStorageService storage,
            RenouvelerOrdonnanceUseCase renouveler
    ) {
        this.creerOrdonnance = creerOrdonnance;
        this.validerOrdonnance = validerOrdonnance;
        this.dispenser = dispenser;
        this.ajouterPiece = ajouterPiece;
        this.ordonnances = ordonnances;
        this.lignes = lignes;
        this.pieces = pieces;
        this.dispensations = dispensations;
        this.produits = produits;
        this.storage = storage;
        this.renouveler = renouveler;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CAISSIER','PHARMACIEN','ADMIN')")
    public CreerOrdonnanceResponse creer(@Valid @RequestBody CreerOrdonnanceRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        String posteNom = PosteContext.posteNom(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        UUID id = creerOrdonnance.execute(new CreerOrdonnanceUseCase.CreerOrdonnanceCommand(
                orgId,
                req.patientId(),
                req.prescripteurId(),
                req.datePrescription(),
                req.dateExpiration(),
                req.ordonnanceParentId(),
                req.lignes().stream().map(l -> new CreerOrdonnanceUseCase.Ligne(l.produitId(), l.quantitePrescrite(), l.posologie(), l.dureeJours())).toList(),
                userId,
                posteNom
        ));
        return new CreerOrdonnanceResponse(id);
    }

    @PostMapping("/{ordonnanceId}/validation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('PHARMACIEN','ADMIN')")
    public void valider(@PathVariable UUID ordonnanceId, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        String posteNom = PosteContext.posteNom(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        validerOrdonnance.valider(orgId, ordonnanceId, userId, posteNom);
    }

    @PostMapping("/{ordonnanceId}/refus")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('PHARMACIEN','ADMIN')")
    public void refuser(@PathVariable UUID ordonnanceId, @Valid @RequestBody RefusRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        String posteNom = PosteContext.posteNom(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        validerOrdonnance.refuser(orgId, ordonnanceId, req.motif(), userId, posteNom);
    }

    @PostMapping("/{ordonnanceId}/dispensations")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('CAISSIER','PHARMACIEN','ADMIN')")
    public void dispenser(@PathVariable UUID ordonnanceId, @Valid @RequestBody DispensationRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        String posteNom = PosteContext.posteNom(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        boolean peutOverrider = hasRole(auth, "ROLE_PHARMACIEN") || hasRole(auth, "ROLE_ADMIN");
        dispenser.execute(new DispenserOrdonnanceUseCase.DispenserCommand(
                orgId,
                ordonnanceId,
                req.ordonnanceLigneId(),
                req.quantite(),
                userId,
                posteNom,
                peutOverrider,
                req.motifOverride()
        ));
    }

    @PostMapping("/{ordonnanceId}/dispensations/preview")
    @PreAuthorize("hasAnyRole('CAISSIER','PHARMACIEN','ADMIN')")
    public DispenserOrdonnanceUseCase.DispensationPreview preview(@PathVariable UUID ordonnanceId, @Valid @RequestBody DispensationPreviewRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        return dispenser.preview(new DispenserOrdonnanceUseCase.PreviewCommand(
                orgId,
                ordonnanceId,
                req.ordonnanceLigneId(),
                req.quantiteSouhaitee()
        ));
    }

    @PostMapping("/{ordonnanceId}/pieces")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CAISSIER','PHARMACIEN','ADMIN')")
    public AjouterPieceResponse ajouterPiece(@PathVariable UUID ordonnanceId, @RequestPart("file") MultipartFile file, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        String posteNom = PosteContext.posteNom(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        UUID pieceId = ajouterPiece.execute(orgId, ordonnanceId, file, userId, posteNom);
        return new AjouterPieceResponse(pieceId);
    }

    @GetMapping("/{ordonnanceId}")
    @PreAuthorize("hasAnyRole('CAISSIER','PHARMACIEN','ADMIN')")
    public OrdonnanceDetailResponse detail(@PathVariable UUID ordonnanceId, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        OrdonnanceJpaEntity o = ordonnances.findByOrganisationIdAndId(orgId, ordonnanceId).orElseThrow();
        var lignesItems = lignes.findByOrganisationIdAndOrdonnanceId(orgId, ordonnanceId).stream()
                .map(l -> {
                    String libelle = produits.findById(l.getProduitId()).map(p -> p.getNomCommercial()).orElse(l.getProduitId().toString());
                    return new OrdonnanceLigneItem(l.getId(), l.getProduitId(), libelle, l.getQuantitePrescrite(), l.getQuantiteDispensee());
                })
                .toList();
        var pieceItems = pieces.findByOrganisationIdAndOrdonnanceId(orgId, ordonnanceId).stream()
                .map(p -> new OrdonnancePieceItem(p.getId(), p.getFichierNom(), p.getContenuType(), p.getStorageKey(), p.getCreatedAt()))
                .toList();
        var dispItems = dispensations.findByOrganisationIdAndOrdonnanceId(orgId, ordonnanceId).stream()
                .map(d -> new DispensationItem(d.getId(), d.getOrdonnanceLigneId(), d.getProduitId(), d.getQuantite(), d.getLotId(), d.getEmplacementId(), d.getCreatedAt(), d.getMotifOverride()))
                .toList();
        return new OrdonnanceDetailResponse(o.getId(), o.getPatientId(), o.getStatut(), o.getDateExpiration(), lignesItems, pieceItems, dispItems);
    }

    @GetMapping("/en-attente-validation")
    @PreAuthorize("hasAnyRole('PHARMACIEN','ADMIN')")
    public List<OrdonnanceResumeResponse> enAttente(JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        return ordonnances.findEnAttenteValidation(orgId).stream()
                .limit(100)
                .map(o -> new OrdonnanceResumeResponse(o.getId(), o.getStatut(), o.getDateExpiration()))
                .toList();
    }

    @GetMapping("/partiellement-dispensees")
    @PreAuthorize("hasAnyRole('PHARMACIEN','ADMIN')")
    public List<OrdonnanceResumeResponse> partiellementDispensees(JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        return ordonnances.findPartiellementDispensees(orgId).stream()
                .limit(200)
                .map(o -> new OrdonnanceResumeResponse(o.getId(), o.getStatut(), o.getDateExpiration()))
                .toList();
    }

    @GetMapping("/{ordonnanceId}/pieces/{pieceId}/download")
    @PreAuthorize("hasAnyRole('CAISSIER','PHARMACIEN','ADMIN')")
    public ResponseEntity<org.springframework.core.io.Resource> downloadPiece(@PathVariable UUID ordonnanceId, @PathVariable UUID pieceId, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        ordonnances.findByOrganisationIdAndId(orgId, ordonnanceId).orElseThrow();
        var piece = pieces.findByOrganisationIdAndId(orgId, pieceId).orElseThrow();
        if (!piece.getOrdonnanceId().equals(ordonnanceId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        var res = storage.loadAsResource(piece.getStorageKey());
        MediaType mt = piece.getContenuType() == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(piece.getContenuType());
        return ResponseEntity.ok()
                .contentType(mt)
                .header("Content-Disposition", "attachment; filename=\"" + piece.getFichierNom().replace("\"", "_") + "\"")
                .body(res);
    }

    @PostMapping("/{ordonnanceId}/renouvellement")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PHARMACIEN','ADMIN')")
    public CreerOrdonnanceResponse renouveler(@PathVariable UUID ordonnanceId, @Valid @RequestBody RenouvellementRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        String posteNom = PosteContext.posteNom(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        UUID newId = renouveler.execute(orgId, ordonnanceId, req.datePrescription(), req.dateExpiration(), userId, posteNom);
        return new CreerOrdonnanceResponse(newId);
    }

    @GetMapping("/patients/{patientId}")
    @PreAuthorize("hasAnyRole('CAISSIER','PHARMACIEN','ADMIN')")
    public List<OrdonnanceResumeResponse> listerParPatient(@PathVariable UUID patientId, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        return ordonnances.findByOrganisationIdAndPatientIdOrderByIdDesc(orgId, patientId).stream()
                .map(o -> new OrdonnanceResumeResponse(o.getId(), o.getStatut(), o.getDateExpiration()))
                .toList();
    }

    private static boolean hasRole(JwtAuthenticationToken auth, String role) {
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(role::equals);
    }

    public record CreerOrdonnanceRequest(
            @NotNull UUID patientId,
            UUID prescripteurId,
            @NotNull LocalDate datePrescription,
            LocalDate dateExpiration,
            UUID ordonnanceParentId,
            @NotNull List<LigneRequest> lignes
    ) {
    }

    public record LigneRequest(@NotNull UUID produitId, @Min(1) int quantitePrescrite, String posologie, Integer dureeJours) {
    }

    public record CreerOrdonnanceResponse(UUID ordonnanceId) {
    }

    public record RefusRequest(@NotBlank String motif) {
    }

    public record DispensationRequest(@NotNull UUID ordonnanceLigneId, @Min(1) int quantite, String motifOverride) {
    }

    public record DispensationPreviewRequest(@NotNull UUID ordonnanceLigneId, @Min(1) int quantiteSouhaitee) {
    }

    public record RenouvellementRequest(@NotNull LocalDate datePrescription, LocalDate dateExpiration) {
    }

    public record AjouterPieceResponse(UUID pieceId) {
    }

    public record OrdonnanceResumeResponse(UUID ordonnanceId, String statut, LocalDate dateExpiration) {
    }

    public record OrdonnanceDetailResponse(
            UUID ordonnanceId,
            UUID patientId,
            String statut,
            LocalDate dateExpiration,
            List<OrdonnanceLigneItem> lignes,
            List<OrdonnancePieceItem> pieces,
            List<DispensationItem> dispensations
    ) {
    }

    public record OrdonnanceLigneItem(UUID ordonnanceLigneId, UUID produitId, String libelleProduit, int quantitePrescrite, int quantiteDispensee) {
    }

    public record OrdonnancePieceItem(UUID pieceId, String fichierNom, String contenuType, String storageKey, java.time.Instant createdAt) {
    }

    public record DispensationItem(UUID dispensationId, UUID ordonnanceLigneId, UUID produitId, int quantite, UUID lotId, UUID emplacementId, java.time.Instant createdAt, String motifOverride) {
    }
}

