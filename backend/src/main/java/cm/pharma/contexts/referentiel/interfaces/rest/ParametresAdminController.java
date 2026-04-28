package cm.pharma.contexts.referentiel.interfaces.rest;

import cm.pharma.contexts.referentiel.application.service.ParametresService;
import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.ParametreJpaRepository;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * API Admin pour consulter et définir les paramètres métiers (par organisation).
 */
@RestController
@RequestMapping("/api/admin/parametres")
public class ParametresAdminController {

    private final ParametresService parametres;
    private final ParametreJpaRepository repo;

    public ParametresAdminController(ParametresService parametres, ParametreJpaRepository repo) {
        this.parametres = parametres;
        this.repo = repo;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ParametreItem> list(JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        return repo.findByOrganisationIdOrderByCleAsc(orgId).stream()
                .map(p -> new ParametreItem(p.getId(), p.getCle(), p.getValeur(), p.getTypeValeur(), p.getDescription(), p.getUpdatedAt()))
                .toList();
    }

    @PutMapping("/{cle}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void upsert(@PathVariable String cle, @Valid @RequestBody UpsertParametreRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        parametres.upsert(orgId, cle, req.valeur(), req.typeValeur(), req.description());
    }

    public record UpsertParametreRequest(
            @NotBlank String valeur,
            String typeValeur,
            String description
    ) {
    }

    public record ParametreItem(UUID id, String cle, String valeur, String typeValeur, String description, java.time.Instant updatedAt) {
    }
}

