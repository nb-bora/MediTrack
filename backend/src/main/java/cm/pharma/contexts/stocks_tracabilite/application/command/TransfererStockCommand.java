package cm.pharma.contexts.stocks_tracabilite.application.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TransfererStockCommand(
        @NotNull UUID organisationId,
        @NotNull UUID produitId,
        @NotNull UUID emplacementSourceId,
        @NotNull UUID emplacementDestinationId,
        @Min(1) int quantite,
        String referenceDocument,
        String motif,
        UUID creePar
) {
}

