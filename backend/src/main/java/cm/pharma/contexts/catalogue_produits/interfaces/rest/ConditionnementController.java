package cm.pharma.contexts.catalogue_produits.interfaces.rest;

import cm.pharma.contexts.catalogue_produits.application.command.AjouterConditionnementCommand;
import cm.pharma.contexts.catalogue_produits.application.command.AjouterConditionnementUseCase;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogue/produits/{produitId}/conditionnements")
public class ConditionnementController {

    private final AjouterConditionnementUseCase ajouterConditionnement;

    public ConditionnementController(AjouterConditionnementUseCase ajouterConditionnement) {
        this.ajouterConditionnement = ajouterConditionnement;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIEN')")
    public AjouterConditionnementResponse add(
            @PathVariable UUID produitId,
            @Valid @RequestBody AjouterConditionnementRequest req,
            JwtAuthenticationToken auth
    ) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID id = ajouterConditionnement.execute(new AjouterConditionnementCommand(
                orgId, produitId, req.nom(), req.uniteBaseNom(), req.quantiteUniteBase(), req.estPrincipal()
        ));
        return new AjouterConditionnementResponse(id);
    }

    public record AjouterConditionnementRequest(
            @NotBlank String nom,
            @NotBlank String uniteBaseNom,
            @Min(1) int quantiteUniteBase,
            boolean estPrincipal
    ) {
    }

    public record AjouterConditionnementResponse(UUID conditionnementId) {
    }
}

