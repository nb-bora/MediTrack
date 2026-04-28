package cm.pharma.contexts.catalogue_produits.application.command;

import cm.pharma.contexts.catalogue_produits.domain.model.TypeProduit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Commande de création de produit (Module C.1).
 */
public record CreerProduitCommand(
        @NotNull UUID organisationId,
        @NotNull TypeProduit typeProduit,
        String dci,
        @NotBlank String nomCommercial,
        String formeGalenique,
        String dosage,
        String laboratoire,
        String paysOrigine,
        UUID categorieId,
        boolean necessiteOrdonnance,
        boolean estStupefiant,
        boolean estPsychotrope,
        boolean estControle,
        UUID profilTaxeId,
        Integer stockMinimum,
        Integer stockSecurite,
        Integer delaiReapproJours
) {
}

