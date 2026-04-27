package cm.pharma.contexts.catalogue_produits.application.command;

import cm.pharma.contexts.catalogue_produits.domain.model.NiveauSubstitut;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record LierSubstitutCommand(
        @NotNull UUID organisationId,
        @NotNull UUID produitId,
        @NotNull UUID substitutProduitId,
        @NotNull NiveauSubstitut niveau,
        UUID creePar
) {
}

