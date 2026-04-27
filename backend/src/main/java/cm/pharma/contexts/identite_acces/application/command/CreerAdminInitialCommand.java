package cm.pharma.contexts.identite_acces.application.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Commande de bootstrap : création du tout premier administrateur.
 *
 * <p>Elle n’est autorisée que si :
 * <ul>
 *   <li>le setup (Module A) est terminé (organisation existe)</li>
 *   <li>aucun utilisateur n’existe encore</li>
 * </ul>
 * </p>
 */
public record CreerAdminInitialCommand(
        @NotBlank String nom,
        @NotBlank String prenom,
        @NotBlank @Size(max = 80) String login,
        @Email String email,
        @Size(max = 30) String telephone
) {
}

