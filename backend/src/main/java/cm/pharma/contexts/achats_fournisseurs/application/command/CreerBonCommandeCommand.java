package cm.pharma.contexts.achats_fournisseurs.application.command;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreerBonCommandeCommand(
        @NotNull UUID organisationId,
        @NotNull UUID fournisseurId,
        LocalDate dateCommande,
        LocalDate dateLivraisonPrevue,
        String commentaire,
        UUID creePar
) {
}

