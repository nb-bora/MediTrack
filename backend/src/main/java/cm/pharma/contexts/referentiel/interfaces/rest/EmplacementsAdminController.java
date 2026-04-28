package cm.pharma.contexts.referentiel.interfaces.rest;

import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.EmplacementJpaEntity;
import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.EmplacementJpaRepository;
import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.SiteJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/referentiel/emplacements")
public class EmplacementsAdminController {

    private final EmplacementJpaRepository emplacements;
    private final SiteJpaRepository sites;

    public EmplacementsAdminController(EmplacementJpaRepository emplacements, SiteJpaRepository sites) {
        this.emplacements = Objects.requireNonNull(emplacements);
        this.sites = Objects.requireNonNull(sites);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<EmplacementItem> list(JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        return emplacements.findByOrganisationIdOrderBySiteNomAndOrdre(orgId).stream()
                .map(e -> new EmplacementItem(
                        e.getId(),
                        e.getSiteId(),
                        e.getCode(),
                        e.getNom(),
                        e.getTypeEmplacement(),
                        e.getOrdreAffichage(),
                        e.isActif(),
                        e.getUpdatedAt()
                ))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public CreateEmplacementResponse create(@Valid @RequestBody CreateEmplacementRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        boolean siteOk = sites.findByOrganisationId(orgId).stream().anyMatch(s -> s.getId().equals(req.siteId()));
        if (!siteOk) {
            throw new BusinessRuleViolationException("Site invalide (hors organisation)");
        }
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        emplacements.save(EmplacementJpaEntity.create(
                id,
                req.siteId(),
                req.code().trim(),
                req.nom().trim(),
                req.typeEmplacement().trim(),
                req.ordreAffichage(),
                now
        ));
        return new CreateEmplacementResponse(id);
    }

    @PutMapping("/{emplacementId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void update(@PathVariable UUID emplacementId, @Valid @RequestBody UpdateEmplacementRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        EmplacementJpaEntity e = emplacements.findById(emplacementId).orElseThrow(() -> new BusinessRuleViolationException("Emplacement introuvable"));

        // Vérifie appartenance via site
        boolean siteOk = sites.findByOrganisationId(orgId).stream().anyMatch(s -> s.getId().equals(e.getSiteId()));
        if (!siteOk) {
            throw new BusinessRuleViolationException("Emplacement hors organisation");
        }
        e.update(req.code().trim(), req.nom().trim(), req.typeEmplacement().trim(), req.ordreAffichage(), req.actif(), Instant.now());
        emplacements.save(e);
    }

    public record CreateEmplacementRequest(
            @NotNull UUID siteId,
            @NotBlank String code,
            @NotBlank String nom,
            @NotBlank String typeEmplacement,
            @Min(0) int ordreAffichage
    ) {
    }

    public record UpdateEmplacementRequest(
            @NotBlank String code,
            @NotBlank String nom,
            @NotBlank String typeEmplacement,
            @Min(0) int ordreAffichage,
            boolean actif
    ) {
    }

    public record CreateEmplacementResponse(UUID emplacementId) {
    }

    public record EmplacementItem(
            UUID id,
            UUID siteId,
            String code,
            String nom,
            String typeEmplacement,
            int ordreAffichage,
            boolean actif,
            Instant updatedAt
    ) {
    }
}

