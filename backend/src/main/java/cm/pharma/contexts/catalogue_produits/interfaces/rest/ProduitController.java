package cm.pharma.contexts.catalogue_produits.interfaces.rest;

import cm.pharma.contexts.catalogue_produits.application.command.CreerProduitCommand;
import cm.pharma.contexts.catalogue_produits.application.command.CreerProduitUseCase;
import cm.pharma.contexts.catalogue_produits.application.command.AjouterCodeBarresCommand;
import cm.pharma.contexts.catalogue_produits.application.command.AjouterCodeBarresUseCase;
import cm.pharma.contexts.catalogue_produits.domain.model.TypeProduit;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaEntity;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import cm.pharma.shared.interfaces.rest.OrganisationContext;

/**
 * API catalogue produits (Module C).
 */
@RestController
@RequestMapping("/api/catalogue/produits")
public class ProduitController {

    private final CreerProduitUseCase creerProduit;
    private final ProduitJpaRepository produits;
    private final AjouterCodeBarresUseCase ajouterCodeBarres;

    public ProduitController(CreerProduitUseCase creerProduit, ProduitJpaRepository produits, AjouterCodeBarresUseCase ajouterCodeBarres) {
        this.creerProduit = creerProduit;
        this.produits = produits;
        this.ajouterCodeBarres = ajouterCodeBarres;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIEN')")
    public CreateProduitResponse create(@Valid @RequestBody CreateProduitRequest req, JwtAuthenticationToken auth) {
        UUID organisationId = OrganisationContext.organisationId(auth);
        var result = creerProduit.execute(new CreerProduitCommand(
                organisationId,
                req.typeProduit(),
                req.dci(),
                req.nomCommercial(),
                req.formeGalenique(),
                req.dosage(),
                req.laboratoire(),
                req.paysOrigine(),
                req.categorieId(),
                req.necessiteOrdonnance(),
                req.estStupefiant(),
                req.estPsychotrope(),
                req.estControle(),
                req.profilTaxeId(),
                req.stockMinimum(),
                req.stockSecurite(),
                req.delaiReapproJours()
        ));
        return new CreateProduitResponse(result.produitId());
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ProduitListItem> list(@RequestParam(required = false) String q, JwtAuthenticationToken auth) {
        UUID organisationId = OrganisationContext.organisationId(auth);
        return produits.search(organisationId, q).stream()
                .map(p -> new ProduitListItem(p.getId(), p.getNomCommercial(), p.getDci(), p.getDosage()))
                .toList();
    }

    @PostMapping("/{produitId}/codes-barres")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIEN')")
    public CreateCodeBarresResponse addCodeBarres(
            @PathVariable UUID produitId,
            @Valid @RequestBody CreateCodeBarresRequest req,
            JwtAuthenticationToken auth
    ) {
        UUID organisationId = OrganisationContext.organisationId(auth);
        UUID id = ajouterCodeBarres.execute(new AjouterCodeBarresCommand(organisationId, produitId, req.ean13(), req.libelle()));
        return new CreateCodeBarresResponse(id);
    }

    public record CreateProduitRequest(
            @NotNull TypeProduit typeProduit,
            String dci,
            @NotNull String nomCommercial,
            String formeGalenique,
            String dosage,
            String laboratoire,
            String paysOrigine,
            UUID categorieId,
            boolean necessiteOrdonnance,
            boolean estStupefiant,
            boolean estPsychotrope,
            boolean estControle,
            @NotNull UUID profilTaxeId,
            Integer stockMinimum,
            Integer stockSecurite,
            Integer delaiReapproJours
    ) {
    }

    public record CreateProduitResponse(UUID produitId) {
    }

    public record ProduitListItem(UUID id, String nomCommercial, String dci, String dosage) {
    }

    public record CreateCodeBarresRequest(
            @NotBlank
            @Pattern(regexp = "^\\d{13}$", message = "EAN13 doit contenir 13 chiffres")
            String ean13,
            String libelle
    ) {
    }

    public record CreateCodeBarresResponse(UUID codeBarresId) {
    }
}

