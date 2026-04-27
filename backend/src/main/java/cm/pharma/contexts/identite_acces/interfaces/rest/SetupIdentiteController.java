package cm.pharma.contexts.identite_acces.interfaces.rest;

import cm.pharma.contexts.identite_acces.application.command.CreerAdminInitialCommand;
import cm.pharma.contexts.identite_acces.application.command.CreerAdminInitialUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de bootstrap pour l’identité.
 *
 * <p>But : créer le premier administrateur après le setup (Module A), sans exposer
 * une création d’utilisateurs “publique” une fois le système en route.</p>
 */
@RestController
@RequestMapping("/api/setup")
public class SetupIdentiteController {

    private final CreerAdminInitialUseCase creerAdminInitial;

    public SetupIdentiteController(CreerAdminInitialUseCase creerAdminInitial) {
        this.creerAdminInitial = creerAdminInitial;
    }

    @PostMapping("/creer-admin-initial")
    @ResponseStatus(HttpStatus.CREATED)
    public CreerAdminInitialResponse creerAdminInitial(@Valid @RequestBody CreerAdminInitialCommand cmd) {
        var result = creerAdminInitial.execute(cmd);
        return new CreerAdminInitialResponse(result.utilisateurId(), result.organisationId(), result.motDePasseTemporaire());
    }

    public record CreerAdminInitialResponse(UUID utilisateurId, UUID organisationId, String motDePasseTemporaire) {
    }
}

