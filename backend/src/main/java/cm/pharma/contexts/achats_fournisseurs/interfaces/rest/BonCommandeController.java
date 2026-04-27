package cm.pharma.contexts.achats_fournisseurs.interfaces.rest;

import cm.pharma.contexts.achats_fournisseurs.application.command.AjouterLigneBonCommandeCommand;
import cm.pharma.contexts.achats_fournisseurs.application.command.AjouterLigneBonCommandeUseCase;
import cm.pharma.contexts.achats_fournisseurs.application.command.CreerBonCommandeCommand;
import cm.pharma.contexts.achats_fournisseurs.application.command.CreerBonCommandeUseCase;
import cm.pharma.contexts.achats_fournisseurs.application.command.ValiderBonCommandeUseCase;
import cm.pharma.contexts.achats_fournisseurs.application.service.BonCommandePdfService;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.BonCommandeJpaRepository;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/achats/bons-commandes")
public class BonCommandeController {

    private final CreerBonCommandeUseCase creerBon;
    private final AjouterLigneBonCommandeUseCase ajouterLigne;
    private final ValiderBonCommandeUseCase validerBon;
    private final BonCommandeJpaRepository bons;
    private final BonCommandePdfService pdfs;

    public BonCommandeController(
            CreerBonCommandeUseCase creerBon,
            AjouterLigneBonCommandeUseCase ajouterLigne,
            ValiderBonCommandeUseCase validerBon,
            BonCommandeJpaRepository bons,
            BonCommandePdfService pdfs
    ) {
        this.creerBon = creerBon;
        this.ajouterLigne = ajouterLigne;
        this.validerBon = validerBon;
        this.bons = bons;
        this.pdfs = pdfs;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MAGASINIER')")
    public CreerBonCommandeResponse creer(@Valid @RequestBody CreerBonCommandeRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        UUID id = creerBon.execute(new CreerBonCommandeCommand(
                orgId, req.fournisseurId(), req.dateCommande(), req.dateLivraisonPrevue(), req.commentaire(), userId
        ));
        return new CreerBonCommandeResponse(id);
    }

    @PostMapping("/{bonCommandeId}/lignes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MAGASINIER')")
    public AjouterLigneResponse ajouterLigne(@PathVariable UUID bonCommandeId, @Valid @RequestBody AjouterLigneRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        UUID id = ajouterLigne.execute(new AjouterLigneBonCommandeCommand(
                orgId, bonCommandeId, req.produitId(), req.quantiteCommandee(), req.prixAttenduUnitaire(), req.devise(), userId
        ));
        return new AjouterLigneResponse(id);
    }

    @PostMapping("/{bonCommandeId}/validation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void valider(@PathVariable UUID bonCommandeId, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        validerBon.execute(orgId, bonCommandeId, userId);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<BonCommandeItem> list(JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        return bons.findByOrganisationIdOrderByCreatedAtDesc(orgId).stream()
                .map(b -> new BonCommandeItem(b.getId(), b.getNumero(), b.getFournisseurId(), b.getStatut()))
                .toList();
    }

    @GetMapping(value = "/{bonCommandeId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> pdf(@PathVariable UUID bonCommandeId, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        byte[] bytes = pdfs.generate(orgId, bonCommandeId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    public record CreerBonCommandeRequest(
            @NotNull UUID fournisseurId,
            LocalDate dateCommande,
            LocalDate dateLivraisonPrevue,
            String commentaire
    ) {
    }

    public record CreerBonCommandeResponse(UUID bonCommandeId) {
    }

    public record AjouterLigneRequest(
            @NotNull UUID produitId,
            @Min(1) int quantiteCommandee,
            BigDecimal prixAttenduUnitaire,
            String devise
    ) {
    }

    public record AjouterLigneResponse(UUID ligneId) {
    }

    public record BonCommandeItem(UUID id, String numero, UUID fournisseurId, String statut) {
    }
}

