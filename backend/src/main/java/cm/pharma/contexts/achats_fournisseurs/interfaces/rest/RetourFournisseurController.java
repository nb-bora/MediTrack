package cm.pharma.contexts.achats_fournisseurs.interfaces.rest;

import cm.pharma.contexts.achats_fournisseurs.application.command.AjouterLigneRetourFournisseurUseCase;
import cm.pharma.contexts.achats_fournisseurs.application.command.CloturerRetourFournisseurUseCase;
import cm.pharma.contexts.achats_fournisseurs.application.command.CreerRetourFournisseurUseCase;
import cm.pharma.contexts.achats_fournisseurs.application.command.MarquerRetourEnvoyeUseCase;
import cm.pharma.contexts.achats_fournisseurs.application.command.ValiderRetourFournisseurUseCase;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/api/achats/retours")
public class RetourFournisseurController {

    private final CreerRetourFournisseurUseCase creerRetour;
    private final AjouterLigneRetourFournisseurUseCase ajouterLigne;
    private final ValiderRetourFournisseurUseCase validerRetour;
    private final MarquerRetourEnvoyeUseCase marquerEnvoye;
    private final CloturerRetourFournisseurUseCase cloturer;

    public RetourFournisseurController(
            CreerRetourFournisseurUseCase creerRetour,
            AjouterLigneRetourFournisseurUseCase ajouterLigne,
            ValiderRetourFournisseurUseCase validerRetour,
            MarquerRetourEnvoyeUseCase marquerEnvoye,
            CloturerRetourFournisseurUseCase cloturer
    ) {
        this.creerRetour = creerRetour;
        this.ajouterLigne = ajouterLigne;
        this.validerRetour = validerRetour;
        this.marquerEnvoye = marquerEnvoye;
        this.cloturer = cloturer;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MAGASINIER')")
    public CreerRetourResponse creer(@Valid @RequestBody CreerRetourRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        UUID id = creerRetour.execute(orgId, req.fournisseurId(), req.motif(), req.referenceDocument(), userId);
        return new CreerRetourResponse(id);
    }

    @PostMapping("/{retourId}/lignes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MAGASINIER')")
    public AjouterLigneRetourResponse ajouterLigne(@PathVariable UUID retourId, @Valid @RequestBody AjouterLigneRetourRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        UUID id = ajouterLigne.execute(orgId, retourId, req.lotId(), req.quantite(), req.motif(), userId);
        return new AjouterLigneRetourResponse(id);
    }

    @PostMapping("/{retourId}/validation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void valider(@PathVariable UUID retourId, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        validerRetour.execute(orgId, retourId, userId);
    }

    @PostMapping("/{retourId}/envoi")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void envoyer(@PathVariable UUID retourId, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        marquerEnvoye.execute(orgId, retourId, userId);
    }

    @PostMapping("/{retourId}/cloture")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void cloturer(@PathVariable UUID retourId, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        this.cloturer.execute(orgId, retourId, userId);
    }

    public record CreerRetourRequest(
            @NotNull UUID fournisseurId,
            @NotBlank String motif,
            String referenceDocument
    ) {
    }

    public record CreerRetourResponse(UUID retourId) {
    }

    public record AjouterLigneRetourRequest(
            @NotNull UUID lotId,
            @Min(1) int quantite,
            String motif
    ) {
    }

    public record AjouterLigneRetourResponse(UUID ligneId) {
    }
}

