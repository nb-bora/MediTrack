package cm.pharma.contexts.catalogue_produits.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.CategorieProduitJpaRepository;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProfilTaxeJpaRepository;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaEntity;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaRepository;
import cm.pharma.contexts.referentiel.application.service.ParametresService;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cas d’usage : création d’un produit avec détection de doublon (DCI + dosage).
 */
@Service
public class CreerProduitUseCase {

    private final ProduitJpaRepository produits;
    private final ProfilTaxeJpaRepository profilsTaxe;
    private final CategorieProduitJpaRepository categories;
    private final ParametresService parametres;
    private final AuditWriter auditWriter;

    public CreerProduitUseCase(
            ProduitJpaRepository produits,
            ProfilTaxeJpaRepository profilsTaxe,
            CategorieProduitJpaRepository categories,
            ParametresService parametres,
            AuditWriter auditWriter
    ) {
        this.produits = Objects.requireNonNull(produits);
        this.profilsTaxe = Objects.requireNonNull(profilsTaxe);
        this.categories = Objects.requireNonNull(categories);
        this.parametres = Objects.requireNonNull(parametres);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public CreerProduitResult execute(CreerProduitCommand cmd) {
        Objects.requireNonNull(cmd, "cmd requis");

        boolean dedupEnabled = parametres.getBoolean(cmd.organisationId(), "PRODUIT_DEDUP_DCI_DOSAGE_ENABLED", true);
        // Détection doublon “sans ambiguïté” : même organisation + DCI + dosage
        if (dedupEnabled && cmd.dci() != null && !cmd.dci().isBlank() && cmd.dosage() != null && !cmd.dosage().isBlank()) {
            List<ProduitJpaEntity> duplicates = produits.findByOrganisationIdAndDciIgnoreCaseAndDosageIgnoreCase(
                    cmd.organisationId(), cmd.dci().trim(), cmd.dosage().trim()
            );
            if (!duplicates.isEmpty()) {
                throw new BusinessRuleViolationException(
                        "Doublon potentiel détecté (même DCI + dosage). Crée un lien de substituabilité au lieu d’une nouvelle fiche."
                );
            }
        }

        UUID profilTaxeId = cmd.profilTaxeId();
        if (profilTaxeId == null) {
            String key = cmd.typeProduit().name().equals("MEDICAMENT")
                    ? "PRODUIT_PROFIL_TAXE_DEFAUT_MEDICAMENTS"
                    : "PRODUIT_PROFIL_TAXE_DEFAUT_PARAPHARMA";
            String defaultNom = cmd.typeProduit().name().equals("MEDICAMENT") ? "MEDICAMENTS" : "PARAPHARMA";
            String profilNom = parametres.getString(cmd.organisationId(), key, defaultNom);
            profilTaxeId = profilsTaxe.findByOrganisationIdAndNom(cmd.organisationId(), profilNom).map(p -> p.getId()).orElse(null);
            if (profilTaxeId == null) {
                throw new BusinessRuleViolationException("Profil TVA par défaut introuvable: " + profilNom);
            }
        }
        if (!profilsTaxe.existsByOrganisationIdAndId(cmd.organisationId(), profilTaxeId)) {
            throw new BusinessRuleViolationException("Profil TVA introuvable pour l'organisation");
        }
        if (cmd.categorieId() != null && !categories.existsByOrganisationIdAndId(cmd.organisationId(), cmd.categorieId())) {
            throw new BusinessRuleViolationException("Catégorie introuvable pour l'organisation");
        }

        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        ProduitJpaEntity created = ProduitJpaEntity.create(
                id,
                new ProduitJpaEntity.ProduitInit(
                        cmd.organisationId(),
                        cmd.typeProduit().name(),
                        cmd.dci(),
                        cmd.nomCommercial(),
                        cmd.formeGalenique(),
                        cmd.dosage(),
                        cmd.laboratoire(),
                        cmd.paysOrigine(),
                        cmd.categorieId(),
                        cmd.necessiteOrdonnance(),
                        cmd.estStupefiant(),
                        cmd.estPsychotrope(),
                        cmd.estControle(),
                        profilTaxeId,
                        cmd.stockMinimum(),
                        cmd.stockSecurite(),
                        cmd.delaiReapproJours()
                ),
                now
        );
        produits.save(created);

        auditWriter.write(AuditEvent.simple(
                cmd.organisationId(), now, null, null, null,
                null, null, "PRODUIT_CREE", "Produit", id.toString(), null,
                Map.of("nom_commercial", cmd.nomCommercial(), "dci", cmd.dci(), "dosage", cmd.dosage())
        ));

        return new CreerProduitResult(id);
    }

    public record CreerProduitResult(UUID produitId) {
    }
}

