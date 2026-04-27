package cm.pharma.contexts.stocks_tracabilite.application.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SaisirInventaireLigneCommand(
        @NotNull UUID organisationId,
        @NotNull UUID inventaireId,
        @NotNull UUID produitId,
        @NotNull UUID emplacementId,
        @Min(0) int stockReel,
        String motifEcart,
        UUID saisiPar
) {
}

