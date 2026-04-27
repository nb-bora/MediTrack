package cm.pharma.contexts.catalogue_produits.interfaces.rest;

import cm.pharma.contexts.catalogue_produits.application.command.CreerPrixCommand;
import cm.pharma.contexts.catalogue_produits.application.command.CreerPrixUseCase;
import cm.pharma.contexts.catalogue_produits.domain.model.TypePrix;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
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
@RequestMapping("/api/catalogue/produits/{produitId}/prix")
public class PrixController {

    private final CreerPrixUseCase creerPrix;

    public PrixController(CreerPrixUseCase creerPrix) {
        this.creerPrix = creerPrix;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public CreerPrixResponse create(
            @PathVariable UUID produitId,
            @Valid @RequestBody CreerPrixRequest req,
            JwtAuthenticationToken auth
    ) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        UUID id = creerPrix.execute(new CreerPrixCommand(
                orgId,
                produitId,
                req.typePrix().name(),
                req.montant(),
                req.devise(),
                req.dateDebut(),
                req.motif(),
                userId
        ));
        return new CreerPrixResponse(id);
    }

    public record CreerPrixRequest(
            @NotNull TypePrix typePrix,
            @NotNull @DecimalMin("0.0") BigDecimal montant,
            @NotNull String devise,
            @NotNull LocalDate dateDebut,
            String motif
    ) {
    }

    public record CreerPrixResponse(UUID prixId) {
    }
}

