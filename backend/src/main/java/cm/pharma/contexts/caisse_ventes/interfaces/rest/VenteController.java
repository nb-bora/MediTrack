package cm.pharma.contexts.caisse_ventes.interfaces.rest;

import cm.pharma.contexts.caisse_ventes.application.command.CreerVenteUseCase;
import cm.pharma.contexts.caisse_ventes.application.command.AjouterPaiementCommand;
import cm.pharma.contexts.caisse_ventes.application.command.AjouterPaiementUseCase;
import cm.pharma.contexts.caisse_ventes.application.command.ScannerEanUseCase;
import cm.pharma.contexts.caisse_ventes.application.command.ValiderVenteUseCase;
import cm.pharma.contexts.caisse_ventes.application.command.AnnulerVenteUseCase;
import cm.pharma.contexts.caisse_ventes.application.command.CreerRetourVenteUseCase;
import cm.pharma.contexts.caisse_ventes.application.command.AppliquerRemiseUseCase;
import cm.pharma.contexts.caisse_ventes.application.command.AppliquerArrondiUseCase;
import cm.pharma.contexts.caisse_ventes.application.command.DefinirTiersPayantVenteUseCase;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import cm.pharma.shared.interfaces.rest.PosteContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/caisse/ventes")
public class VenteController {

    private final CreerVenteUseCase creerVente;
    private final ScannerEanUseCase scannerEan;
    private final AjouterPaiementUseCase ajouterPaiement;
    private final ValiderVenteUseCase validerVente;
    private final AnnulerVenteUseCase annulerVente;
    private final CreerRetourVenteUseCase creerRetourVente;
    private final AppliquerRemiseUseCase appliquerRemise;
    private final AppliquerArrondiUseCase appliquerArrondi;
    private final DefinirTiersPayantVenteUseCase definirTiersPayant;

    public VenteController(
            CreerVenteUseCase creerVente,
            ScannerEanUseCase scannerEan,
            AjouterPaiementUseCase ajouterPaiement,
            ValiderVenteUseCase validerVente,
            AnnulerVenteUseCase annulerVente,
            CreerRetourVenteUseCase creerRetourVente,
            AppliquerRemiseUseCase appliquerRemise,
            AppliquerArrondiUseCase appliquerArrondi,
            DefinirTiersPayantVenteUseCase definirTiersPayant
    ) {
        this.creerVente = creerVente;
        this.scannerEan = scannerEan;
        this.ajouterPaiement = ajouterPaiement;
        this.validerVente = validerVente;
        this.annulerVente = annulerVente;
        this.creerRetourVente = creerRetourVente;
        this.appliquerRemise = appliquerRemise;
        this.appliquerArrondi = appliquerArrondi;
        this.definirTiersPayant = definirTiersPayant;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CAISSIER','PHARMACIEN','ADMIN')")
    public CreerVenteResponse creer(JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        String posteNom = PosteContext.posteNom(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        UUID id = creerVente.execute(orgId, posteNom, userId, "XAF");
        return new CreerVenteResponse(id);
    }

    @PostMapping("/{venteId}/scan")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CAISSIER','PHARMACIEN','ADMIN')")
    public ScanResponse scan(@PathVariable UUID venteId, @Valid @RequestBody ScanRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        String posteNom = PosteContext.posteNom(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        UUID ligneId = scannerEan.execute(orgId, venteId, req.ean13(), req.quantite(), userId, posteNom);
        return new ScanResponse(ligneId);
    }

    @PostMapping("/{venteId}/paiements")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CAISSIER','PHARMACIEN','ADMIN')")
    public PaiementResponse payer(@PathVariable UUID venteId, @Valid @RequestBody PaiementRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        String posteNom = PosteContext.posteNom(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        UUID paiementId = ajouterPaiement.execute(new AjouterPaiementCommand(
                orgId,
                venteId,
                req.modePaiement(),
                req.montant(),
                req.reference(),
                userId,
                posteNom
        ));
        return new PaiementResponse(paiementId);
    }

    @PostMapping("/{venteId}/validation")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('CAISSIER','PHARMACIEN','ADMIN')")
    public ValiderVenteUseCase.ValiderVenteResult valider(@PathVariable UUID venteId, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        String posteNom = PosteContext.posteNom(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        boolean peutValiderOrdonnance = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_PHARMACIEN") || a.equals("ROLE_ADMIN"));
        return validerVente.execute(orgId, venteId, userId, posteNom, peutValiderOrdonnance);
    }

    @PostMapping("/{venteId}/remises")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('CAISSIER','PHARMACIEN','ADMIN')")
    public void remise(@PathVariable UUID venteId, @Valid @RequestBody RemiseRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        String posteNom = PosteContext.posteNom(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        boolean isCaissier = hasRole(auth, "ROLE_CAISSIER");
        boolean isPharmacien = hasRole(auth, "ROLE_PHARMACIEN");
        boolean isAdmin = hasRole(auth, "ROLE_ADMIN");
        appliquerRemise.execute(orgId, venteId, req.venteLigneId(), req.remisePct(), req.motif(), userId, posteNom, isCaissier, isPharmacien, isAdmin);
    }

    @PostMapping("/{venteId}/arrondi")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('CAISSIER','PHARMACIEN','ADMIN')")
    public void arrondi(@PathVariable UUID venteId, @Valid @RequestBody ArrondiRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        String posteNom = PosteContext.posteNom(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        boolean isCaissier = hasRole(auth, "ROLE_CAISSIER");
        appliquerArrondi.execute(orgId, venteId, req.montant(), userId, posteNom, isCaissier);
    }

    @PostMapping("/{venteId}/tiers-payant")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('CAISSIER','PHARMACIEN','ADMIN')")
    public void definirTiersPayant(@PathVariable UUID venteId, @Valid @RequestBody TiersPayantRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        String posteNom = PosteContext.posteNom(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        definirTiersPayant.execute(new DefinirTiersPayantVenteUseCase.DefinirTiersPayantCommand(
                orgId,
                venteId,
                req.patientId(),
                req.organismeId(),
                req.ordonnanceId(),
                req.numeroAdherent(),
                userId,
                posteNom
        ));
    }

    @PostMapping("/{venteId}/annulation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('PHARMACIEN','ADMIN')")
    public void annuler(@PathVariable UUID venteId, @Valid @RequestBody AnnulerRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        String posteNom = PosteContext.posteNom(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        annulerVente.execute(orgId, venteId, userId, posteNom, req.motif());
    }

    @PostMapping("/{venteId}/retours")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PHARMACIEN','ADMIN')")
    public CreerRetourVenteUseCase.RetourVenteResult retour(@PathVariable UUID venteId, @Valid @RequestBody RetourRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        String posteNom = PosteContext.posteNom(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        return creerRetourVente.execute(new CreerRetourVenteUseCase.CreerRetourVenteCommand(
                orgId,
                venteId,
                req.motif(),
                req.modeRemboursement(),
                req.reference(),
                req.lignes().stream().map(l -> new CreerRetourVenteUseCase.RetourLigne(l.venteLigneId(), l.quantite())).toList(),
                userId,
                posteNom
        ));
    }

    public record CreerVenteResponse(UUID venteId) {
    }

    public record ScanRequest(@NotBlank String ean13, @Min(1) int quantite) {
    }

    public record ScanResponse(UUID venteLigneId) {
    }

    public record PaiementRequest(@NotBlank String modePaiement, @NotNull BigDecimal montant, String reference) {
    }

    public record PaiementResponse(UUID paiementId) {
    }

    public record AnnulerRequest(@NotBlank String motif) {
    }

    public record RetourRequest(
            @NotBlank String motif,
            @NotBlank String modeRemboursement,
            String reference,
            @NotNull List<RetourLigneRequest> lignes
    ) {
    }

    public record RetourLigneRequest(@NotNull UUID venteLigneId, @Min(1) int quantite) {
    }

    public record RemiseRequest(@NotNull UUID venteLigneId, @NotNull @DecimalMin("0.0") BigDecimal remisePct, String motif) {
    }

    public record ArrondiRequest(@NotNull @DecimalMin("0.0") BigDecimal montant) {
    }

    public record TiersPayantRequest(
            @NotNull UUID patientId,
            @NotNull UUID organismeId,
            UUID ordonnanceId,
            String numeroAdherent
    ) {
    }

    private static boolean hasRole(JwtAuthenticationToken auth, String role) {
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(role::equals);
    }
}

