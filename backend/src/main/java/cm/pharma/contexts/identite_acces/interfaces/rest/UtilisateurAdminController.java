package cm.pharma.contexts.identite_acces.interfaces.rest;

import cm.pharma.contexts.identite_acces.application.command.AdminGererUtilisateurUseCase;
import cm.pharma.contexts.identite_acces.application.command.CreerUtilisateurUseCase;
import cm.pharma.contexts.identite_acces.infrastructure.persistence.jpa.UtilisateurJpaRepository;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import cm.pharma.shared.interfaces.rest.PosteContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurAdminController {

    private final CreerUtilisateurUseCase creer;
    private final AdminGererUtilisateurUseCase gerer;
    private final UtilisateurJpaRepository utilisateurs;

    public UtilisateurAdminController(CreerUtilisateurUseCase creer, AdminGererUtilisateurUseCase gerer, UtilisateurJpaRepository utilisateurs) {
        this.creer = creer;
        this.gerer = gerer;
        this.utilisateurs = utilisateurs;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public CreerUtilisateurResponse creer(
            @Valid @RequestBody CreerUtilisateurRequest req,
            JwtAuthenticationToken auth,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip
    ) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID actorId = UUID.fromString(auth.getToken().getSubject());
        String posteNom = PosteContext.posteNom(auth);
        var res = creer.execute(new CreerUtilisateurUseCase.CreerUtilisateurCommand(
                orgId,
                req.nom(),
                req.prenom(),
                req.login(),
                req.email(),
                req.telephone(),
                req.roleCode(),
                actorId,
                posteNom,
                ip
        ));
        return new CreerUtilisateurResponse(res.utilisateurId(), res.motDePasseTemporaire());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UtilisateurItem> lister(JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        return utilisateurs.findByOrganisationIdOrderByNomAscPrenomAsc(orgId).stream()
                .map(u -> new UtilisateurItem(u.getId(), u.getNom(), u.getPrenom(), u.getLogin(), u.isActif(), u.getTentativesEchec(), u.getVerrouilleJusqua()))
                .toList();
    }

    @PutMapping("/{utilisateurId}/desactiver")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void desactiver(@PathVariable UUID utilisateurId, JwtAuthenticationToken auth,
                           @RequestHeader(value = "X-Forwarded-For", required = false) String ip) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID actorId = UUID.fromString(auth.getToken().getSubject());
        String posteNom = PosteContext.posteNom(auth);
        gerer.setActif(orgId, utilisateurId, false, actorId, posteNom, ip);
    }

    @PutMapping("/{utilisateurId}/activer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void activer(@PathVariable UUID utilisateurId, JwtAuthenticationToken auth,
                        @RequestHeader(value = "X-Forwarded-For", required = false) String ip) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID actorId = UUID.fromString(auth.getToken().getSubject());
        String posteNom = PosteContext.posteNom(auth);
        gerer.setActif(orgId, utilisateurId, true, actorId, posteNom, ip);
    }

    @PutMapping("/{utilisateurId}/deverrouiller")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deverrouiller(@PathVariable UUID utilisateurId, JwtAuthenticationToken auth,
                              @RequestHeader(value = "X-Forwarded-For", required = false) String ip) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID actorId = UUID.fromString(auth.getToken().getSubject());
        String posteNom = PosteContext.posteNom(auth);
        gerer.deverrouiller(orgId, utilisateurId, actorId, posteNom, ip);
    }

    @PostMapping("/{utilisateurId}/reset-mot-de-passe")
    @PreAuthorize("hasRole('ADMIN')")
    public ResetMdpResponse resetMotDePasse(@PathVariable UUID utilisateurId, JwtAuthenticationToken auth,
                                           @RequestHeader(value = "X-Forwarded-For", required = false) String ip) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID actorId = UUID.fromString(auth.getToken().getSubject());
        String posteNom = PosteContext.posteNom(auth);
        var res = gerer.resetMotDePasse(orgId, utilisateurId, actorId, posteNom, ip);
        return new ResetMdpResponse(res.motDePasseTemporaire());
    }

    public record CreerUtilisateurRequest(
            @NotBlank String nom,
            @NotBlank String prenom,
            @NotBlank String login,
            String email,
            String telephone,
            @NotBlank String roleCode
    ) {
    }

    public record CreerUtilisateurResponse(UUID utilisateurId, String motDePasseTemporaire) {
    }

    public record ResetMdpResponse(String motDePasseTemporaire) {
    }

    public record UtilisateurItem(UUID id, String nom, String prenom, String login, boolean actif, int tentativesEchec, java.time.Instant verrouilleJusqua) {
    }
}

