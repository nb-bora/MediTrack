package cm.pharma.contexts.achats_fournisseurs.interfaces.rest;

import cm.pharma.contexts.achats_fournisseurs.application.command.ValiderPrixReceptionUseCase;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
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
@RequestMapping("/api/achats/receptions")
public class ReceptionValidationController {

    private final ValiderPrixReceptionUseCase validerPrix;

    public ReceptionValidationController(ValiderPrixReceptionUseCase validerPrix) {
        this.validerPrix = validerPrix;
    }

    @PostMapping("/{receptionId}/prix/validation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public void valider(@PathVariable UUID receptionId, @RequestBody MessageRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        validerPrix.execute(orgId, receptionId, true, req.message(), userId);
    }

    @PostMapping("/{receptionId}/prix/refus")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public void refuser(@PathVariable UUID receptionId, @RequestBody MessageRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        validerPrix.execute(orgId, receptionId, false, req.message(), userId);
    }

    public record MessageRequest(@NotBlank String message) {
    }
}

