package cm.pharma.contexts.stocks_tracabilite.interfaces.rest;

import cm.pharma.contexts.stocks_tracabilite.application.command.CreerRappelLotCommand;
import cm.pharma.contexts.stocks_tracabilite.application.command.CreerRappelLotUseCase;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stocks/rappels")
public class RappelController {

    private final CreerRappelLotUseCase creerRappel;

    public RappelController(CreerRappelLotUseCase creerRappel) {
        this.creerRappel = creerRappel;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIEN')")
    public RappelResponse create(@Valid @RequestBody RappelRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        UUID id = creerRappel.execute(new CreerRappelLotCommand(orgId, req.lotId(), req.criticite(), req.motif(), req.source(), userId));
        return new RappelResponse(id);
    }

    public record RappelRequest(
            @NotNull UUID lotId,
            @NotBlank String criticite,
            @NotBlank String motif,
            String source
    ) {
    }

    public record RappelResponse(UUID rappelId) {
    }
}

