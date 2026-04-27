package cm.pharma.contexts.achats_fournisseurs.application.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record EnregistrerReceptionCommand(
        @NotNull UUID organisationId,
        UUID bonCommandeId,
        @NotNull UUID fournisseurId,
        String referenceDocument,
        @NotNull List<LigneReception> lignes,
        UUID creePar
) {
    public record LigneReception(
            @NotNull UUID produitId,
            @NotNull UUID emplacementDestinationId,
            @NotBlank String numeroLot,
            @NotNull LocalDate datePeremption,
            @Min(1) int quantiteRecue,
            BigDecimal prixFactureUnitaire,
            String devise,
            Double temperatureTransportC,
            boolean confirmerPeremptionProche
    ) {
    }
}

