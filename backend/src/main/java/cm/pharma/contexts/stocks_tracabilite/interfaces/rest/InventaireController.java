package cm.pharma.contexts.stocks_tracabilite.interfaces.rest;

import cm.pharma.contexts.stocks_tracabilite.application.command.CreerInventaireUseCase;
import cm.pharma.contexts.stocks_tracabilite.application.command.SaisirInventaireLigneCommand;
import cm.pharma.contexts.stocks_tracabilite.application.command.SaisirInventaireLigneUseCase;
import cm.pharma.contexts.stocks_tracabilite.application.command.ValiderInventaireUseCase;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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
@RequestMapping("/api/stocks/inventaires")
public class InventaireController {

    private final CreerInventaireUseCase creerInventaire;
    private final SaisirInventaireLigneUseCase saisirLigne;
    private final ValiderInventaireUseCase validerInventaire;

    public InventaireController(
            CreerInventaireUseCase creerInventaire,
            SaisirInventaireLigneUseCase saisirLigne,
            ValiderInventaireUseCase validerInventaire
    ) {
        this.creerInventaire = creerInventaire;
        this.saisirLigne = saisirLigne;
        this.validerInventaire = validerInventaire;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIEN')")
    public CreerInventaireResponse creer(JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        UUID id = creerInventaire.execute(orgId, userId);
        return new CreerInventaireResponse(id);
    }

    @PostMapping("/{inventaireId}/lignes/saisie")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('PHARMACIEN','MAGASINIER','ADMIN')")
    public void saisir(@PathVariable UUID inventaireId, @Valid @RequestBody SaisieLigneRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        saisirLigne.execute(new SaisirInventaireLigneCommand(
                orgId,
                inventaireId,
                req.produitId(),
                req.emplacementId(),
                req.stockReel(),
                req.motifEcart(),
                userId
        ));
    }

    @PostMapping("/{inventaireId}/validation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIEN')")
    public void valider(@PathVariable UUID inventaireId, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        validerInventaire.execute(orgId, inventaireId, userId);
    }

    public record CreerInventaireResponse(UUID inventaireId) {
    }

    public record SaisieLigneRequest(
            @NotNull UUID produitId,
            @NotNull UUID emplacementId,
            @Min(0) int stockReel,
            String motifEcart
    ) {
    }
}

