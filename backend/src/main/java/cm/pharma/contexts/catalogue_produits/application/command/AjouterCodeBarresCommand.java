package cm.pharma.contexts.catalogue_produits.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

/**
 * Commande d’ajout d’un code-barres EAN13 à un produit.
 */
public record AjouterCodeBarresCommand(
        @NotNull UUID produitId,
        @NotBlank
        @Pattern(regexp = "^\\d{13}$", message = "EAN13 doit contenir 13 chiffres")
        String ean13,
        String libelle
) {
}

