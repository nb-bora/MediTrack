package cm.pharma.contexts.caisse_ventes.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteJpaRepository;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteLigneJpaEntity;
import cm.pharma.contexts.caisse_ventes.infrastructure.persistence.jpa.VenteLigneJpaRepository;
import cm.pharma.contexts.catalogue_produits.domain.model.TypePrix;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.CodeBarresProduitJpaRepository;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.PrixProduitJpaRepository;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaEntity;
import cm.pharma.contexts.catalogue_produits.infrastructure.persistence.jpa.ProduitJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScannerEanUseCase {

    private final VenteJpaRepository ventes;
    private final VenteLigneJpaRepository lignes;
    private final CodeBarresProduitJpaRepository codesBarres;
    private final ProduitJpaRepository produits;
    private final PrixProduitJpaRepository prix;
    private final AuditWriter auditWriter;

    public ScannerEanUseCase(
            VenteJpaRepository ventes,
            VenteLigneJpaRepository lignes,
            CodeBarresProduitJpaRepository codesBarres,
            ProduitJpaRepository produits,
            PrixProduitJpaRepository prix,
            AuditWriter auditWriter
    ) {
        this.ventes = Objects.requireNonNull(ventes);
        this.lignes = Objects.requireNonNull(lignes);
        this.codesBarres = Objects.requireNonNull(codesBarres);
        this.produits = Objects.requireNonNull(produits);
        this.prix = Objects.requireNonNull(prix);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public UUID execute(UUID organisationId, UUID venteId, String ean13, int quantite, UUID actorId, String posteNom) {
        if (ean13 == null || ean13.isBlank()) {
            throw new BusinessRuleViolationException("EAN requis");
        }
        if (quantite <= 0) {
            throw new BusinessRuleViolationException("Quantité invalide");
        }
        VenteJpaEntity vente = ventes.findByOrganisationIdAndId(organisationId, venteId)
                .orElseThrow(() -> new BusinessRuleViolationException("Vente introuvable"));
        if (!"BROUILLON".equals(vente.getStatut())) {
            throw new BusinessRuleViolationException("Vente non modifiable");
        }

        UUID produitId = codesBarres.findByEan13(ean13.trim())
                .map(cb -> cb.getProduitId())
                .orElseThrow(() -> new BusinessRuleViolationException("Code-barres inconnu"));

        ProduitJpaEntity produit = produits.findById(produitId)
                .orElseThrow(() -> new BusinessRuleViolationException("Produit introuvable"));
        if (!produit.getOrganisationId().equals(organisationId)) {
            throw new BusinessRuleViolationException("Produit hors organisation");
        }

        var p = prix.findApplicableAt(produitId, TypePrix.VENTE.name(), LocalDate.now());
        if (p.isEmpty()) {
            throw new BusinessRuleViolationException("Aucun prix de vente actif pour ce produit");
        }
        BigDecimal prixUnitaire = p.get(0).getMontant();

        VenteLigneJpaEntity ligne = lignes.findByVenteIdAndProduitId(venteId, produitId).orElse(null);
        if (ligne == null) {
            UUID id = UUID.randomUUID();
            BigDecimal total = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
            lignes.save(VenteLigneJpaEntity.create(new VenteLigneJpaEntity.VenteLigneInit(
                    id,
                    venteId,
                    organisationId,
                    produitId,
                    quantite,
                    prixUnitaire,
                    BigDecimal.ZERO,
                    total
            )));
            ligne = lignes.findById(id).orElseThrow();
        } else {
            ligne.incrementerQuantite(quantite, prixUnitaire);
        }

        Instant now = Instant.now();
        auditWriter.write(AuditEvent.simple(
                organisationId, now, actorId, null, null,
                posteNom, null, "VENTE_LIGNE_SCANNED", "Vente", vente.getNumeroVente(), null,
                Map.of("ean13", ean13, "produit_id", produitId, "quantite", quantite)
        ));

        return ligne.getId();
    }
}

