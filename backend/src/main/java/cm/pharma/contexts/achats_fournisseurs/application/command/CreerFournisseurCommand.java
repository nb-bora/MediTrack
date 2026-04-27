package cm.pharma.contexts.achats_fournisseurs.application.command;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreerFournisseurCommand(
        UUID organisationId,
        @NotBlank String raisonSociale,
        String numeroRc,
        String numeroContribuable,
        String adresse,
        String contactNom,
        String contactTelephone,
        String emailCommandes,
        String modePaiementPrefere,
        UUID creePar
) {
}

