package cm.pharma.contexts.referentiel.interfaces.rest;

import cm.pharma.contexts.referentiel.application.command.initialisation.InitialiserOrganisationCommand;
import cm.pharma.contexts.referentiel.application.command.initialisation.InitialiserOrganisationUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * API “premier démarrage”.
 *
 * <p>Correspond au Module A (assistant de configuration). Cette route est volontairement
 * publique au tout début, car aucun compte n’existe encore. Une fois l’initialisation faite,
 * l’API doit refuser une 2e initialisation.</p>
 */
@RestController
@RequestMapping("/api/setup")
public class SetupController {

    private final InitialiserOrganisationUseCase initialiserOrganisation;

    public SetupController(InitialiserOrganisationUseCase initialiserOrganisation) {
        this.initialiserOrganisation = initialiserOrganisation;
    }

    @PostMapping("/initialiser")
    @ResponseStatus(HttpStatus.CREATED)
    public SetupResponse initialiser(@Valid @RequestBody InitialiserOrganisationCommand cmd) {
        UUID organisationId = initialiserOrganisation.execute(cmd);
        return new SetupResponse(organisationId);
    }

    public record SetupResponse(UUID organisationId) {
    }
}

