package cm.pharma.contexts.caisse_ventes.interfaces.rest;

import cm.pharma.contexts.caisse_ventes.application.command.FermerSessionCaisseUseCase;
import cm.pharma.contexts.caisse_ventes.application.command.OuvrirSessionCaisseUseCase;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import cm.pharma.shared.interfaces.rest.PosteContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
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
@RequestMapping("/api/caisse")
public class CaisseController {

    private final OuvrirSessionCaisseUseCase ouvrir;
    private final FermerSessionCaisseUseCase fermer;

    public CaisseController(OuvrirSessionCaisseUseCase ouvrir, FermerSessionCaisseUseCase fermer) {
        this.ouvrir = ouvrir;
        this.fermer = fermer;
    }

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CAISSIER','COMPTABLE','ADMIN')")
    public OuvrirSessionResponse ouvrir(@Valid @RequestBody OuvrirSessionRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        String posteNom = PosteContext.posteNom(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        UUID sessionId = ouvrir.execute(orgId, posteNom, userId, req.fondInitial(), req.devise());
        return new OuvrirSessionResponse(sessionId);
    }

    @PostMapping("/sessions/{sessionId}/fermeture")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('COMPTABLE','ADMIN')")
    public void fermer(@PathVariable UUID sessionId, @Valid @RequestBody FermerSessionRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        String posteNom = PosteContext.posteNom(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        fermer.execute(orgId, sessionId, posteNom, userId, req.montantReel(), req.motifEcart());
    }

    public record OuvrirSessionRequest(
            @NotNull @DecimalMin("0.0") BigDecimal fondInitial,
            String devise
    ) {
    }

    public record OuvrirSessionResponse(UUID sessionCaisseId) {
    }

    public record FermerSessionRequest(
            @NotNull @DecimalMin("0.0") BigDecimal montantReel,
            String motifEcart
    ) {
    }
}

