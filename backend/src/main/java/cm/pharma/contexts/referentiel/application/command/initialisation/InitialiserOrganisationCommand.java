package cm.pharma.contexts.referentiel.application.command.initialisation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Commande d’initialisation “premier démarrage”.
 *
 * <p>Cette commande est la base de l’assistant (wizard) décrit dans le Module A.
 * Tant qu’elle n’a pas été exécutée avec succès, l’application doit rester bloquée
 * (pas de création d’utilisateurs, pas de ventes, etc.).</p>
 */
public record InitialiserOrganisationCommand(
        @NotBlank String nomCommercial,
        @NotBlank @Size(max = 50) String numeroAutorisationOuverture,
        @NotBlank String adresse,
        @NotBlank @Size(max = 30) String telephone,
        @Email String email,
        @NotBlank String responsableLegalNom,
        String responsableLegalNumeroOrdre,
        @NotBlank @Size(min = 3, max = 3) String devise,
        @NotNull BigDecimal tvaMedicaments,
        @NotNull BigDecimal tvaParapharma,
        @NotBlank String formatFacture,
        @NotBlank String formatTicket
) {
}

