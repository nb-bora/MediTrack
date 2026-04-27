package cm.pharma.contexts.stocks_tracabilite.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreerRappelLotCommand(
        @NotNull UUID organisationId,
        @NotNull UUID lotId,
        @NotBlank String criticite,
        @NotBlank String motif,
        String source,
        UUID creePar
) {
}

