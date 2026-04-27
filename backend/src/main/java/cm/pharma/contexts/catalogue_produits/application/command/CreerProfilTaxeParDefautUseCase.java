package cm.pharma.contexts.catalogue_produits.application.command;

import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProfilTaxeJpaEntity;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProfilTaxeJpaRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crée les profils TVA par défaut d’une organisation si absents.
 *
 * <p>Exemple Cameroun V1 :
 * <ul>
 *   <li>Médicaments : 0%</li>
 *   <li>Para-pharmaceutique : 19.25%</li>
 * </ul>
 * </p>
 */
@Service
public class CreerProfilTaxeParDefautUseCase {

    public static final String PROFIL_MEDICAMENTS = "MEDICAMENTS";
    public static final String PROFIL_PARAPHARMA = "PARAPHARMA";

    private final ProfilTaxeJpaRepository profils;

    public CreerProfilTaxeParDefautUseCase(ProfilTaxeJpaRepository profils) {
        this.profils = Objects.requireNonNull(profils);
    }

    @Transactional
    public void execute(UUID organisationId, BigDecimal tvaMedicaments, BigDecimal tvaParapharma) {
        Instant now = Instant.now();
        profils.findByOrganisationIdAndNom(organisationId, PROFIL_MEDICAMENTS)
                .orElseGet(() -> profils.save(ProfilTaxeJpaEntity.create(UUID.randomUUID(), organisationId, PROFIL_MEDICAMENTS, tvaMedicaments, now)));

        profils.findByOrganisationIdAndNom(organisationId, PROFIL_PARAPHARMA)
                .orElseGet(() -> profils.save(ProfilTaxeJpaEntity.create(UUID.randomUUID(), organisationId, PROFIL_PARAPHARMA, tvaParapharma, now)));
    }
}

