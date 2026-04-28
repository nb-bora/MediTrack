package cm.pharma.contexts.catalogue_produits.application.command;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreerPrixCommand(
        @NotNull UUID organisationId,
        @NotNull UUID produitId,
        @NotBlank String typePrix,
        @NotNull @DecimalMin("0.0") BigDecimal montant,
        String devise,
        @NotNull LocalDate dateDebut,
        String motif,
        UUID creePar
) {
}

