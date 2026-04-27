package cm.pharma.contexts.ordonnances_dossier_patient.interfaces.rest;

import cm.pharma.contexts.ordonnances_dossier_patient.application.command.CreerPrescripteurUseCase;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.PrescripteurJpaRepository;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prescripteurs")
public class PrescripteurController {

    private final CreerPrescripteurUseCase creerPrescripteur;
    private final PrescripteurJpaRepository prescripteurs;

    public PrescripteurController(CreerPrescripteurUseCase creerPrescripteur, PrescripteurJpaRepository prescripteurs) {
        this.creerPrescripteur = creerPrescripteur;
        this.prescripteurs = prescripteurs;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PHARMACIEN','ADMIN')")
    public CreerPrescripteurResponse creer(@Valid @RequestBody CreerPrescripteurRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        String posteNom = PosteContext.posteNom(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        UUID id = creerPrescripteur.execute(new CreerPrescripteurUseCase.CreerPrescripteurCommand(
                orgId,
                req.nom(),
                req.structure(),
                req.telephone(),
                userId,
                posteNom
        ));
        return new CreerPrescripteurResponse(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CAISSIER','PHARMACIEN','ADMIN')")
    public List<PrescripteurItem> lister(JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        return prescripteurs.findByOrganisationIdOrderByNomAsc(orgId).stream()
                .map(p -> new PrescripteurItem(p.getId(), p.getNom(), p.getStructure(), p.getTelephone()))
                .toList();
    }

    public record CreerPrescripteurRequest(@NotBlank String nom, String structure, String telephone) {
    }

    public record CreerPrescripteurResponse(UUID prescripteurId) {
    }

    public record PrescripteurItem(UUID id, String nom, String structure, String telephone) {
    }
}

