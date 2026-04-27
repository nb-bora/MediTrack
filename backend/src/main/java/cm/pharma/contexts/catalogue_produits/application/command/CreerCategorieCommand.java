package cm.pharma.contexts.catalogue_produits.application.command;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreerCategorieCommand(
        UUID organisationId,
        UUID parentId,
        @NotBlank String nom
) {
}

