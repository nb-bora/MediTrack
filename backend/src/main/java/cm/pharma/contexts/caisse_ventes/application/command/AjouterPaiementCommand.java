package cm.pharma.contexts.caisse_ventes.application.command;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record AjouterPaiementCommand(
        @NotNull UUID organisationId,
        @NotNull UUID venteId,
        @NotBlank String modePaiement,
        @NotNull @DecimalMin("0.0") BigDecimal montant,
        String reference,
        UUID creePar,
        String posteNom
) {
}

