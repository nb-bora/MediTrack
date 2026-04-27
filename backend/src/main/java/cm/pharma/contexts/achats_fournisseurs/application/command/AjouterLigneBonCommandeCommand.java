package cm.pharma.contexts.achats_fournisseurs.application.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record AjouterLigneBonCommandeCommand(
        @NotNull UUID organisationId,
        @NotNull UUID bonCommandeId,
        @NotNull UUID produitId,
        @Min(1) int quantiteCommandee,
        BigDecimal prixAttenduUnitaire,
        String devise,
        UUID creePar
) {
}

