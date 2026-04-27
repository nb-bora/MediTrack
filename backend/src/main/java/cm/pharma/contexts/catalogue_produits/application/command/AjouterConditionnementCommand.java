package cm.pharma.contexts.catalogue_produits.application.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AjouterConditionnementCommand(
        @NotNull UUID organisationId,
        @NotNull UUID produitId,
        @NotBlank String nom,
        @NotBlank String uniteBaseNom,
        @Min(1) int quantiteUniteBase,
        boolean estPrincipal
) {
}

