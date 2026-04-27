package cm.pharma.contexts.achats_fournisseurs.interfaces.rest;

import cm.pharma.contexts.achats_fournisseurs.application.command.CreerFournisseurCommand;
import cm.pharma.contexts.achats_fournisseurs.application.command.CreerFournisseurUseCase;
import cm.pharma.contexts.achats_fournisseurs.application.command.ModifierFournisseurUseCase;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.FournisseurJpaRepository;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/achats/fournisseurs")
public class FournisseurController {

    private final CreerFournisseurUseCase creerFournisseur;
    private final ModifierFournisseurUseCase modifierFournisseur;
    private final FournisseurJpaRepository fournisseurs;

    public FournisseurController(CreerFournisseurUseCase creerFournisseur, ModifierFournisseurUseCase modifierFournisseur, FournisseurJpaRepository fournisseurs) {
        this.creerFournisseur = creerFournisseur;
        this.modifierFournisseur = modifierFournisseur;
        this.fournisseurs = fournisseurs;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public CreateFournisseurResponse create(@Valid @RequestBody CreateFournisseurRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        UUID id = creerFournisseur.execute(new CreerFournisseurCommand(
                orgId,
                req.raisonSociale(),
                req.numeroRc(),
                req.numeroContribuable(),
                req.adresse(),
                req.contactNom(),
                req.contactTelephone(),
                req.emailCommandes(),
                req.modePaiementPrefere(),
                userId
        ));
        return new CreateFournisseurResponse(id);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<FournisseurItem> list(JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        return fournisseurs.findByOrganisationIdOrderByRaisonSocialeAsc(orgId).stream()
                .map(f -> new FournisseurItem(f.getId(), f.getRaisonSociale(), f.isActif()))
                .toList();
    }

    @PutMapping("/{fournisseurId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void update(@PathVariable UUID fournisseurId, @Valid @RequestBody CreateFournisseurRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        modifierFournisseur.execute(orgId, fournisseurId, new CreerFournisseurCommand(
                orgId,
                req.raisonSociale(),
                req.numeroRc(),
                req.numeroContribuable(),
                req.adresse(),
                req.contactNom(),
                req.contactTelephone(),
                req.emailCommandes(),
                req.modePaiementPrefere(),
                userId
        ), userId);
    }

    public record CreateFournisseurRequest(
            @NotBlank String raisonSociale,
            String numeroRc,
            String numeroContribuable,
            String adresse,
            String contactNom,
            String contactTelephone,
            String emailCommandes,
            String modePaiementPrefere
    ) {
    }

    public record CreateFournisseurResponse(UUID fournisseurId) {
    }

    public record FournisseurItem(UUID id, String raisonSociale, boolean actif) {
    }
}

