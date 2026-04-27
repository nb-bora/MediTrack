package cm.pharma.shared.interfaces.rest;

import cm.pharma.shared.application.AlerteService;
import cm.pharma.shared.infrastructure.persistence.jpa.AlerteJpaRepository;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alertes")
public class AlerteController {

    private final AlerteJpaRepository alertes;
    private final AlerteService service;

    public AlerteController(AlerteJpaRepository alertes, AlerteService service) {
        this.alertes = alertes;
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AlerteItem> list(JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        return alertes.findByOrganisationIdAndResolvedAtIsNullOrderByCreatedAtDesc(orgId).stream()
                .map(a -> new AlerteItem(a.getId(), a.getTypeAlerte(), a.getEntite(), a.getEntiteId()))
                .toList();
    }

    @PostMapping("/{alerteId}/resolution")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void resolve(@PathVariable UUID alerteId, @RequestBody ResolveRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        service.resolve(orgId, alerteId, userId, req.message());
    }

    public record ResolveRequest(@NotBlank String message) {
    }

    public record AlerteItem(UUID id, String type, String entite, String entiteId) {
    }
}

