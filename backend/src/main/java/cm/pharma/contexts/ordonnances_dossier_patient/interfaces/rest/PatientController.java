package cm.pharma.contexts.ordonnances_dossier_patient.interfaces.rest;

import cm.pharma.contexts.ordonnances_dossier_patient.application.command.CreerPatientUseCase;
import cm.pharma.contexts.ordonnances_dossier_patient.application.command.MettreAJourPatientMedicalUseCase;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.PatientJpaRepository;
import cm.pharma.contexts.ordonnances_dossier_patient.infrastructure.persistence.jpa.PatientMedicalJpaRepository;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import cm.pharma.shared.interfaces.rest.PosteContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final CreerPatientUseCase creerPatient;
    private final MettreAJourPatientMedicalUseCase majMedical;
    private final PatientJpaRepository patients;
    private final PatientMedicalJpaRepository medical;

    public PatientController(
            CreerPatientUseCase creerPatient,
            MettreAJourPatientMedicalUseCase majMedical,
            PatientJpaRepository patients,
            PatientMedicalJpaRepository medical
    ) {
        this.creerPatient = creerPatient;
        this.majMedical = majMedical;
        this.patients = patients;
        this.medical = medical;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CAISSIER','PHARMACIEN','ADMIN')")
    public CreerPatientResponse creer(@Valid @RequestBody CreerPatientRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        String posteNom = PosteContext.posteNom(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        UUID id = creerPatient.execute(new CreerPatientUseCase.CreerPatientCommand(
                orgId,
                req.nom(),
                req.prenom(),
                req.dateNaissance(),
                req.sexe(),
                req.telephone(),
                req.adresse(),
                req.assuranceOrganismeNom(),
                req.assuranceNumeroAdherent(),
                req.assuranceTauxCouverture(),
                userId,
                posteNom
        ));
        return new CreerPatientResponse(id);
    }

    @PutMapping("/{patientId}/medical")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('PHARMACIEN','ADMIN')")
    public void majMedical(@PathVariable UUID patientId, @Valid @RequestBody MajMedicalRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        String posteNom = PosteContext.posteNom(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        majMedical.execute(new MettreAJourPatientMedicalUseCase.MettreAJourPatientMedicalCommand(
                orgId,
                patientId,
                req.allergies(),
                req.pathologiesChroniques(),
                req.medecinTraitant(),
                userId,
                posteNom
        ));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CAISSIER','PHARMACIEN','ADMIN')")
    public List<PatientListItem> lister(@RequestParam(name = "q", required = false) String q, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        var src = (q == null || q.isBlank())
                ? patients.findByOrganisationIdOrderByNomAscPrenomAsc(orgId)
                : patients.search(orgId, q.trim());
        // Limite simple côté API pour l'écran caisse.
        return src.stream()
                .limit(50)
                .map(p -> new PatientListItem(
                        p.getId(),
                        p.getNom(),
                        p.getPrenom(),
                        p.getTelephone(),
                        p.getAssuranceOrganismeNom(),
                        p.getAssuranceNumeroAdherent(),
                        p.getAssuranceTauxCouverture()
                ))
                .toList();
    }

    @GetMapping("/{patientId}")
    @PreAuthorize("hasAnyRole('CAISSIER','PHARMACIEN','ADMIN')")
    public PatientDetailResponse detail(@PathVariable UUID patientId, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        var p = patients.findByOrganisationIdAndId(orgId, patientId).orElseThrow();
        return new PatientDetailResponse(
                p.getId(),
                p.getNom(),
                p.getPrenom(),
                p.getDateNaissance(),
                p.getSexe(),
                p.getTelephone(),
                p.getAdresse(),
                p.getAssuranceOrganismeNom(),
                p.getAssuranceNumeroAdherent(),
                p.getAssuranceTauxCouverture()
        );
    }

    @GetMapping("/{patientId}/medical")
    @PreAuthorize("hasAnyRole('PHARMACIEN','ADMIN')")
    public MedicalView medical(@PathVariable UUID patientId, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        patients.findByOrganisationIdAndId(orgId, patientId).orElseThrow();
        return medical.findByPatientId(patientId)
                .map(m -> new MedicalView(m.getAllergies(), m.getPathologiesChroniques(), m.getMedecinTraitant()))
                .orElse(new MedicalView(null, null, null));
    }

    public record CreerPatientRequest(
            @NotBlank String nom,
            @NotBlank String prenom,
            LocalDate dateNaissance,
            String sexe,
            String telephone,
            String adresse,
            String assuranceOrganismeNom,
            String assuranceNumeroAdherent,
            Double assuranceTauxCouverture
    ) {
    }

    public record CreerPatientResponse(UUID patientId) {
    }

    public record MajMedicalRequest(
            String allergies,
            String pathologiesChroniques,
            String medecinTraitant
    ) {
    }

    public record MedicalView(String allergies, String pathologiesChroniques, String medecinTraitant) {
    }

    public record PatientListItem(
            UUID patientId,
            String nom,
            String prenom,
            String telephone,
            String assuranceOrganismeNom,
            String assuranceNumeroAdherent,
            Double assuranceTauxCouverture
    ) {
    }

    public record PatientDetailResponse(
            UUID patientId,
            String nom,
            String prenom,
            LocalDate dateNaissance,
            String sexe,
            String telephone,
            String adresse,
            String assuranceOrganismeNom,
            String assuranceNumeroAdherent,
            Double assuranceTauxCouverture
    ) {
    }
}

