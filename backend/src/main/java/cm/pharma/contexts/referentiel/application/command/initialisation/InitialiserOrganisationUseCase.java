package cm.pharma.contexts.referentiel.application.command.initialisation;

import cm.pharma.shared.application.ClockPort;
import cm.pharma.shared.application.UseCase;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.EmplacementJpaEntity;
import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.EmplacementJpaRepository;
import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.OrganisationJpaEntity;
import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.OrganisationJpaRepository;
import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.ParametreJpaEntity;
import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.ParametreJpaRepository;
import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.SequenceNumerotationJpaEntity;
import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.SequenceNumerotationJpaRepository;
import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.SiteJpaEntity;
import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.SiteJpaRepository;
import cm.pharma.contexts.catalogue_produits.application.command.CreerProfilTaxeParDefautUseCase;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cas d’usage : exécute l’assistant de premier démarrage (Module A.1).
 *
 * <p>Exigence technique : les accès aux données passent par JPA (multi-SGBD) et non par SQL brut.</p>
 */
@Service
public class InitialiserOrganisationUseCase implements UseCase<InitialiserOrganisationCommand, UUID> {

    private static final String TYPE_STRING = "STRING";
    private static final String TYPE_NUMBER = "NUMBER";

    private final OrganisationJpaRepository organisations;
    private final SiteJpaRepository sites;
    private final EmplacementJpaRepository emplacements;
    private final ParametreJpaRepository parametres;
    private final SequenceNumerotationJpaRepository sequences;
    private final CreerProfilTaxeParDefautUseCase creerProfilsTaxeParDefaut;
    private final ClockPort clock;

    public InitialiserOrganisationUseCase(
            OrganisationJpaRepository organisations,
            SiteJpaRepository sites,
            EmplacementJpaRepository emplacements,
            ParametreJpaRepository parametres,
            SequenceNumerotationJpaRepository sequences,
            CreerProfilTaxeParDefautUseCase creerProfilsTaxeParDefaut,
            ClockPort clock
    ) {
        this.organisations = Objects.requireNonNull(organisations);
        this.sites = Objects.requireNonNull(sites);
        this.emplacements = Objects.requireNonNull(emplacements);
        this.parametres = Objects.requireNonNull(parametres);
        this.sequences = Objects.requireNonNull(sequences);
        this.creerProfilsTaxeParDefaut = Objects.requireNonNull(creerProfilsTaxeParDefaut);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    @Transactional
    public UUID execute(InitialiserOrganisationCommand cmd) {
        Objects.requireNonNull(cmd, "cmd est requis");

        if (organisations.count() > 0) {
            throw new BusinessRuleViolationException("Initialisation déjà effectuée : une organisation existe déjà");
        }
        if (organisations.existsByNumeroAutorisationOuverture(cmd.numeroAutorisationOuverture())) {
            throw new BusinessRuleViolationException("Numéro d’autorisation déjà utilisé");
        }

        UUID organisationId = UUID.randomUUID();
        Instant now = clock.now();

        organisations.save(OrganisationJpaEntity.create(
                organisationId,
                new OrganisationJpaEntity.OrganisationInit(
                        cmd.nomCommercial(),
                        cmd.numeroAutorisationOuverture(),
                        cmd.adresse(),
                        cmd.telephone(),
                        cmd.email(),
                        cmd.responsableLegalNom(),
                        cmd.responsableLegalNumeroOrdre(),
                        cmd.devise()
                ),
                now
        ));

        // Un site “par défaut” : la plupart des pharmacies V1 sont mono-site.
        UUID siteId = UUID.randomUUID();
        sites.save(SiteJpaEntity.create(siteId, organisationId, "Site principal", cmd.adresse(), cmd.telephone(), now));

        // Emplacements standards (Module A.1 - Étape 4)
        emplacements.save(EmplacementJpaEntity.create(UUID.randomUUID(), siteId, "RESERVE", "Réserve Principale", "RESERVE", 10, now));
        emplacements.save(EmplacementJpaEntity.create(UUID.randomUUID(), siteId, "COMPTOIR_MED", "Comptoir Médicaments", "COMPTOIR", 20, now));
        emplacements.save(EmplacementJpaEntity.create(UUID.randomUUID(), siteId, "COMPTOIR_PARA", "Comptoir Para-pharmaceutique", "COMPTOIR", 30, now));
        emplacements.save(EmplacementJpaEntity.create(UUID.randomUUID(), siteId, "FRIGO", "Chambre Froide / Frigo", "FROID", 40, now));
        emplacements.save(EmplacementJpaEntity.create(UUID.randomUUID(), siteId, "STUPEFIANTS", "Local Stupéfiants", "SECURISE", 50, now));
        emplacements.save(EmplacementJpaEntity.create(UUID.randomUUID(), siteId, "QUARANTAINE", "Zone Quarantaine", "QUARANTAINE", 60, now));

        // Paramètres fiscaux / alertes (Module A.1 - Étape 3 & 5)
        parametres.save(ParametreJpaEntity.create(UUID.randomUUID(), organisationId, "DEVISE", cmd.devise(), TYPE_STRING, "Devise principale", now));
        parametres.save(ParametreJpaEntity.create(UUID.randomUUID(), organisationId, "TVA_MEDICAMENTS", cmd.tvaMedicaments().toPlainString(), TYPE_NUMBER, "Taux TVA médicaments", now));
        parametres.save(ParametreJpaEntity.create(UUID.randomUUID(), organisationId, "TVA_PARAPHARMA", cmd.tvaParapharma().toPlainString(), TYPE_NUMBER, "Taux TVA para-pharmaceutique", now));
        parametres.save(ParametreJpaEntity.create(UUID.randomUUID(), organisationId, "ALERTE_STOCK_BAS_JOURS", "30", TYPE_NUMBER, "Seuil alerte stock bas (jours)", now));
        parametres.save(ParametreJpaEntity.create(UUID.randomUUID(), organisationId, "ALERTE_PEREMPTION_PRECOCE_JOURS", "90", TYPE_NUMBER, "Alerte péremption précoce (jours)", now));
        parametres.save(ParametreJpaEntity.create(UUID.randomUUID(), organisationId, "ALERTE_PEREMPTION_URGENTE_JOURS", "30", TYPE_NUMBER, "Alerte péremption urgente (jours)", now));

        // Séquences (Module A.2)
        sequences.save(SequenceNumerotationJpaEntity.create(UUID.randomUUID(), organisationId, "VENTE", "VT-{AA}-{MM}-{SEQ5}", now));
        sequences.save(SequenceNumerotationJpaEntity.create(UUID.randomUUID(), organisationId, "FACTURE", "FAC-{AAAA}-{SEQ6}", now));
        sequences.save(SequenceNumerotationJpaEntity.create(UUID.randomUUID(), organisationId, "TICKET", cmd.formatTicket(), now));
        sequences.save(SequenceNumerotationJpaEntity.create(UUID.randomUUID(), organisationId, "BON_COMMANDE", "BC-{AA}-{SEQ4}", now));
        sequences.save(SequenceNumerotationJpaEntity.create(UUID.randomUUID(), organisationId, "RECEPTION", "REC-{AA}-{MM}-{SEQ4}", now));
        sequences.save(SequenceNumerotationJpaEntity.create(UUID.randomUUID(), organisationId, "INVENTAIRE", "INV-{AA}-{MM}-{SEQ2}", now));
        sequences.save(SequenceNumerotationJpaEntity.create(UUID.randomUUID(), organisationId, "ORDONNANCE", "ORD-{AA}-{MM}-{SEQ5}", now));

        // Module C — profils TVA par défaut (pour rendre le catalogue immédiatement utilisable)
        creerProfilsTaxeParDefaut.execute(organisationId, cmd.tvaMedicaments(), cmd.tvaParapharma());

        return organisationId;
    }
}

