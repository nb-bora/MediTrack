package cm.pharma.contexts.audit_tracabilite.interfaces.rest;

import cm.pharma.contexts.audit_tracabilite.infrastructure.persistence.jpa.EvenementAuditJpaRepository;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final EvenementAuditJpaRepository audits;

    public AuditController(EvenementAuditJpaRepository audits) {
        this.audits = audits;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditItem> search(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entite,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            JwtAuthenticationToken auth
    ) {
        UUID orgId = OrganisationContext.organisationId(auth);
        if ((action == null || action.isBlank())
                && (entite == null || entite.isBlank())
                && from == null
                && to == null) {
            return audits.findTop200ByOrganisationIdOrderByHorodatageDesc(orgId).stream()
                    .map(AuditItem::from)
                    .toList();
        }
        return audits.search(
                        orgId,
                        action == null || action.isBlank() ? null : action.trim(),
                        entite == null || entite.isBlank() ? null : entite.trim(),
                        from,
                        to
                ).stream()
                .limit(500)
                .map(AuditItem::from)
                .toList();
    }

    public record AuditItem(
            UUID id,
            Instant horodatage,
            UUID utilisateurId,
            String utilisateurNom,
            String utilisateurRole,
            String poste,
            String adresseIp,
            String action,
            String entite,
            String entiteId,
            String motif,
            String details
    ) {
        static AuditItem from(cm.pharma.contexts.audit_tracabilite.infrastructure.persistence.jpa.EvenementAuditJpaEntity e) {
            return new AuditItem(
                    e.getId(),
                    e.getHorodatage(),
                    e.getUtilisateurId(),
                    e.getUtilisateurNom(),
                    e.getUtilisateurRole(),
                    e.getPoste(),
                    e.getAdresseIp(),
                    e.getAction(),
                    e.getEntite(),
                    e.getEntiteId(),
                    e.getMotif(),
                    e.getDetails()
            );
        }
    }
}

