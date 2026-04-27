package cm.pharma.contexts.catalogue_produits.interfaces.rest;

import cm.pharma.contexts.catalogue_produits.application.command.LierSubstitutCommand;
import cm.pharma.contexts.catalogue_produits.application.command.LierSubstitutUseCase;
import cm.pharma.contexts.catalogue_produits.domain.model.NiveauSubstitut;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping("/api/catalogue/produits/{produitId}/substituts")
public class SubstitutController {

    private final LierSubstitutUseCase lierSubstitut;

    public SubstitutController(LierSubstitutUseCase lierSubstitut) {
        this.lierSubstitut = lierSubstitut;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIEN')")
    public void link(
            @PathVariable UUID produitId,
            @Valid @RequestBody LierSubstitutRequest req,
            JwtAuthenticationToken auth
    ) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        lierSubstitut.execute(new LierSubstitutCommand(orgId, produitId, req.substitutProduitId(), req.niveau(), userId));
    }

    public record LierSubstitutRequest(
            @NotNull UUID substitutProduitId,
            @NotNull NiveauSubstitut niveau
    ) {
    }
}

