package cm.pharma.contexts.achats_fournisseurs.application.service;

import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.BonCommandeJpaEntity;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.BonCommandeJpaRepository;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.BonCommandeLigneJpaRepository;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.FournisseurJpaEntity;
import cm.pharma.contexts.achats_fournisseurs.infrastructure.persistence.jpa.FournisseurJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BonCommandePdfService {

    private final BonCommandeJpaRepository bons;
    private final BonCommandeLigneJpaRepository lignes;
    private final FournisseurJpaRepository fournisseurs;

    public BonCommandePdfService(BonCommandeJpaRepository bons, BonCommandeLigneJpaRepository lignes, FournisseurJpaRepository fournisseurs) {
        this.bons = Objects.requireNonNull(bons);
        this.lignes = Objects.requireNonNull(lignes);
        this.fournisseurs = Objects.requireNonNull(fournisseurs);
    }

    @Transactional(readOnly = true)
    public byte[] generate(UUID organisationId, UUID bonCommandeId) {
        BonCommandeJpaEntity bc = bons.findByOrganisationIdAndId(organisationId, bonCommandeId)
                .orElseThrow(() -> new BusinessRuleViolationException("Bon de commande introuvable"));
        FournisseurJpaEntity f = fournisseurs.findByOrganisationIdAndId(organisationId, bc.getFournisseurId())
                .orElseThrow(() -> new BusinessRuleViolationException("Fournisseur introuvable"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document();
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font title = new Font(Font.HELVETICA, 16, Font.BOLD);
            doc.add(new Paragraph("BON DE COMMANDE", title));
            doc.add(new Paragraph("Numéro: " + bc.getNumero()));
            doc.add(new Paragraph("Fournisseur: " + f.getRaisonSociale()));
            if (bc.getDateLivraisonPrevue() != null) {
                doc.add(new Paragraph("Livraison prévue: " + bc.getDateLivraisonPrevue().format(DateTimeFormatter.ISO_DATE)));
            }
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(3);
            table.addCell("Produit ID");
            table.addCell("Quantité commandée");
            table.addCell("Quantité reçue");
            for (var l : lignes.findByBonCommandeId(bonCommandeId)) {
                table.addCell(l.getProduitId().toString());
                table.addCell(Integer.toString(l.getQuantiteCommandee()));
                table.addCell(Integer.toString(l.getQuantiteRecue()));
            }
            doc.add(table);

            doc.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Erreur génération PDF bon de commande", e);
        }
    }
}

