package cm.pharma.contexts.achats_fournisseurs.interfaces.rest;

import cm.pharma.contexts.achats_fournisseurs.application.command.EnregistrerReceptionCommand;
import cm.pharma.contexts.achats_fournisseurs.application.command.EnregistrerReceptionUseCase;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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
@RequestMapping("/api/achats/receptions")
public class ReceptionController {

    private final EnregistrerReceptionUseCase enregistrerReception;

    public ReceptionController(EnregistrerReceptionUseCase enregistrerReception) {
        this.enregistrerReception = enregistrerReception;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MAGASINIER')")
    public ReceptionResponse create(@Valid @RequestBody ReceptionRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        UUID id = enregistrerReception.execute(new EnregistrerReceptionCommand(
                orgId,
                req.bonCommandeId(),
                req.fournisseurId(),
                req.referenceDocument(),
                req.lignes(),
                userId
        ));
        return new ReceptionResponse(id);
    }

    public record ReceptionRequest(
            UUID bonCommandeId,
            @NotNull UUID fournisseurId,
            String referenceDocument,
            @NotNull List<EnregistrerReceptionCommand.LigneReception> lignes
    ) {
    }

    public record ReceptionResponse(UUID receptionId) {
    }
}

