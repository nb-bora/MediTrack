package cm.pharma.contexts.achats_fournisseurs.interfaces.rest;

import cm.pharma.contexts.achats_fournisseurs.application.query.FournisseurKpiQueryService;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/achats/fournisseurs/{fournisseurId}/kpis")
public class FournisseurKpiController {

    private final FournisseurKpiQueryService kpis;

    public FournisseurKpiController(FournisseurKpiQueryService kpis) {
        this.kpis = kpis;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public FournisseurKpiQueryService.FournisseurKpis get(@PathVariable UUID fournisseurId, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        return kpis.compute(orgId, fournisseurId);
    }
}

