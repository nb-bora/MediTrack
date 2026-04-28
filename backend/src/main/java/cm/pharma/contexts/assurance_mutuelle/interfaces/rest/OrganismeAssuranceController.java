package cm.pharma.contexts.assurance_mutuelle.interfaces.rest;

import cm.pharma.contexts.assurance_mutuelle.application.command.CreerOrganismeAssuranceUseCase;
import cm.pharma.contexts.assurance_mutuelle.application.command.DefinirCouvertureOrganismeUseCase;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.OrganismeAssuranceJpaRepository;
import cm.pharma.contexts.assurance_mutuelle.infrastructure.persistence.jpa.OrganismeCouvertureJpaRepository;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import cm.pharma.shared.interfaces.rest.PosteContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assurances/organismes")
public class OrganismeAssuranceController {

    private final CreerOrganismeAssuranceUseCase creer;
    private final DefinirCouvertureOrganismeUseCase definirCouverture;
    private final OrganismeAssuranceJpaRepository organismes;
    private final OrganismeCouvertureJpaRepository couvertures;

    public OrganismeAssuranceController(
            CreerOrganismeAssuranceUseCase creer,
            DefinirCouvertureOrganismeUseCase definirCouverture,
            OrganismeAssuranceJpaRepository organismes,
            OrganismeCouvertureJpaRepository couvertures
    ) {
        this.creer = creer;
        this.definirCouverture = definirCouverture;
        this.organismes = organismes;
        this.couvertures = couvertures;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN')")
    public CreerOrganismeResponse creer(@Valid @RequestBody CreerOrganismeRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        String posteNom = PosteContext.posteNom(auth);
        UUID id = creer.execute(new CreerOrganismeAssuranceUseCase.CreerOrganismeCommand(
                orgId,
                req.code(),
                req.nom(),
                req.type(),
                req.frequenceFacturation(),
                req.delaiPaiementJours(),
                userId,
                posteNom
        ));
        return new CreerOrganismeResponse(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE','PHARMACIEN')")
    public List<OrganismeItem> lister(JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        return organismes.findByOrganisationIdOrderByNomAsc(orgId).stream()
                .map(o -> new OrganismeItem(o.getId(), o.getCode(), o.getNom(), o.getType(), o.getFrequenceFacturation(), o.getDelaiPaiementJours()))
                .toList();
    }

    @PutMapping("/{organismeId}/couverture")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN')")
    public void definirCouverture(@PathVariable UUID organismeId, @Valid @RequestBody DefinirCouvertureRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        String posteNom = PosteContext.posteNom(auth);
        definirCouverture.execute(new DefinirCouvertureOrganismeUseCase.DefinirCouvertureCommand(
                orgId,
                organismeId,
                req.tauxGenerique(),
                req.tauxMarque(),
                req.tauxParapharma(),
                req.tauxStupefiants(),
                req.plafondJournalier(),
                req.plafondMensuel(),
                req.plafondAnnuel(),
                req.pieceOrdonnanceOriginale(),
                req.pieceCarteAdherent(),
                req.pieceBonPriseEnCharge(),
                req.pieceExamens(),
                userId,
                posteNom
        ));
    }

    @GetMapping("/{organismeId}/couverture")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE','PHARMACIEN')")
    public CouvertureResponse getCouverture(@PathVariable UUID organismeId, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        var c = couvertures.findByOrganisationIdAndOrganismeId(orgId, organismeId).orElse(null);
        if (c == null) {
            return null;
        }
        return new CouvertureResponse(
                c.getTauxGenerique(),
                c.getTauxMarque(),
                c.getTauxParapharma(),
                c.getTauxStupefiants(),
                c.getPlafondJournalier(),
                c.getPlafondMensuel(),
                c.getPlafondAnnuel(),
                c.isPieceOrdonnanceOriginale(),
                c.isPieceCarteAdherent(),
                c.isPieceBonPriseEnCharge(),
                c.isPieceExamens()
        );
    }

    public record CreerOrganismeRequest(@NotBlank String code, @NotBlank String nom, @NotBlank String type, String frequenceFacturation, int delaiPaiementJours) {
    }

    public record CreerOrganismeResponse(UUID organismeId) {
    }

    public record OrganismeItem(UUID id, String code, String nom, String type, String frequenceFacturation, int delaiPaiementJours) {
    }

    public record DefinirCouvertureRequest(
            @NotNull BigDecimal tauxGenerique,
            @NotNull BigDecimal tauxMarque,
            @NotNull BigDecimal tauxParapharma,
            @NotNull BigDecimal tauxStupefiants,
            BigDecimal plafondJournalier,
            BigDecimal plafondMensuel,
            BigDecimal plafondAnnuel,
            boolean pieceOrdonnanceOriginale,
            boolean pieceCarteAdherent,
            boolean pieceBonPriseEnCharge,
            boolean pieceExamens
    ) {
    }

    public record CouvertureResponse(
            BigDecimal tauxGenerique,
            BigDecimal tauxMarque,
            BigDecimal tauxParapharma,
            BigDecimal tauxStupefiants,
            BigDecimal plafondJournalier,
            BigDecimal plafondMensuel,
            BigDecimal plafondAnnuel,
            boolean pieceOrdonnanceOriginale,
            boolean pieceCarteAdherent,
            boolean pieceBonPriseEnCharge,
            boolean pieceExamens
    ) {
    }
}

