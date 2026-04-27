package cm.pharma.contexts.catalogue_produits.interfaces.rest;

import cm.pharma.contexts.catalogue_produits.application.command.CreerCategorieCommand;
import cm.pharma.contexts.catalogue_produits.application.command.CreerCategorieUseCase;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.CategorieProduitJpaRepository;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogue/categories")
public class CategorieController {

    private final CreerCategorieUseCase creerCategorie;
    private final CategorieProduitJpaRepository categories;

    public CategorieController(CreerCategorieUseCase creerCategorie, CategorieProduitJpaRepository categories) {
        this.creerCategorie = creerCategorie;
        this.categories = categories;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIEN')")
    public CreateCategorieResponse create(@Valid @RequestBody CreateCategorieRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID id = creerCategorie.execute(new CreerCategorieCommand(orgId, req.parentId(), req.nom()));
        return new CreateCategorieResponse(id);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<CategorieListItem> list(JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        return categories.findByOrganisationIdOrderByNomAsc(orgId).stream()
                .map(c -> new CategorieListItem(c.getId()))
                .toList();
    }

    public record CreateCategorieRequest(UUID parentId, @NotBlank String nom) {
    }

    public record CreateCategorieResponse(UUID categorieId) {
    }

    public record CategorieListItem(UUID id) {
    }
}

