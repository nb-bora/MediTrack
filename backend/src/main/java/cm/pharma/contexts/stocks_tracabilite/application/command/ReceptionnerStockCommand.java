package cm.pharma.contexts.stocks_tracabilite.application.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReceptionnerStockCommand(
        @NotNull UUID organisationId,
        @NotNull UUID produitId,
        @NotNull UUID emplacementDestinationId,
        @NotBlank String numeroLot,
        @NotNull LocalDate datePeremption,
        @Min(1) int quantite,
        BigDecimal prixAchatUnitaire,
        Integer quantiteCommandee,
        BigDecimal prixAttenduUnitaire,
        BigDecimal prixFactureUnitaire,
        Double temperatureTransportC,
        boolean confirmerPeremptionProche,
        String referenceDocument,
        String motif,
        UUID creePar
) {
}

