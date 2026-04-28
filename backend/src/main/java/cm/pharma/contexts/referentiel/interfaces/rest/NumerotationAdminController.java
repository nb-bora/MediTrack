package cm.pharma.contexts.referentiel.interfaces.rest;

import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.SequenceNumerotationJpaEntity;
import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.SequenceNumerotationJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/api/admin/referentiel/numerotation")
public class NumerotationAdminController {

    private final SequenceNumerotationJpaRepository sequences;

    public NumerotationAdminController(SequenceNumerotationJpaRepository sequences) {
        this.sequences = Objects.requireNonNull(sequences);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<SequenceItem> list(JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        return sequences.findByOrganisationIdOrderByTypeDocumentAsc(orgId).stream()
                .map(s -> new SequenceItem(
                        s.getId(),
                        s.getTypeDocument(),
                        s.getFormat(),
                        s.getCompteurCourant(),
                        s.getResetFrequence(),
                        s.getResetDernier(),
                        s.getUpdatedAt()
                ))
                .toList();
    }

    @PutMapping("/{typeDocument}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void update(@PathVariable String typeDocument, @Valid @RequestBody UpdateSequenceRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        SequenceNumerotationJpaEntity seq = sequences.findByOrganisationIdAndTypeDocument(orgId, typeDocument.trim().toUpperCase())
                .orElseThrow(() -> new BusinessRuleViolationException("Séquence introuvable"));
        seq.updateConfig(req.format().trim(), req.resetFrequence(), Instant.now());
        sequences.save(seq);
    }

    @PostMapping("/{typeDocument}/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void reset(@PathVariable String typeDocument, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        SequenceNumerotationJpaEntity seq = sequences.findByOrganisationIdAndTypeDocument(orgId, typeDocument.trim().toUpperCase())
                .orElseThrow(() -> new BusinessRuleViolationException("Séquence introuvable"));
        seq.resetCounter(Instant.now());
        sequences.save(seq);
    }

    public record UpdateSequenceRequest(
            @NotBlank String format,
            String resetFrequence
    ) {
    }

    public record SequenceItem(
            UUID id,
            String typeDocument,
            String format,
            int compteurCourant,
            String resetFrequence,
            Instant resetDernier,
            Instant updatedAt
    ) {
    }
}

