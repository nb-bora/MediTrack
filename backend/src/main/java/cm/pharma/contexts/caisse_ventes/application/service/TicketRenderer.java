package cm.pharma.contexts.caisse_ventes.application.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public final class TicketRenderer {
    private TicketRenderer() {
    }

    private static final String SEP = "============================================\n";
    private static final String LINE = "--------------------------------------------\n";

    public static String render(TicketHeader h, List<TicketLine> lignes, BigDecimal totalPaye, BigDecimal monnaieRendue) {
        Objects.requireNonNull(h);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.systemDefault());
        StringBuilder sb = new StringBuilder();
        sb.append(SEP);
        sb.append("TICKET DE CAISSE\n");
        sb.append(SEP);
        sb.append("Date : ").append(dtf.format(Instant.now())).append("\n");
        sb.append("Poste : ").append(h.posteNom()).append("\n");
        sb.append("Vente : ").append(h.numeroVente()).append("\n");
        sb.append("Ticket : ").append(h.numeroTicket()).append("\n");
        sb.append(LINE);
        for (TicketLine l : lignes) {
            sb.append(l.libelle()).append(" x").append(l.quantite()).append("  ").append(l.totalLigne()).append("\n");
        }
        sb.append(LINE);
        BigDecimal arrondi = h.arrondi() == null ? BigDecimal.ZERO : h.arrondi();
        BigDecimal totalNet = h.totalTtc().subtract(arrondi);
        if (totalNet.compareTo(BigDecimal.ZERO) < 0) {
            totalNet = BigDecimal.ZERO;
        }
        sb.append("TOTAL TTC ").append(h.totalTtc()).append("\n");
        if (arrondi.compareTo(BigDecimal.ZERO) > 0) {
            sb.append("ARRONDI -").append(arrondi).append("\n");
        }
        sb.append("TOTAL A PAYER ").append(totalNet).append("\n");
        sb.append("PAYE ").append(totalPaye).append("\n");
        sb.append("MONNAIE ").append(monnaieRendue).append("\n");
        sb.append(SEP);
        sb.append("Merci de votre confiance !\n");
        sb.append(SEP);
        return sb.toString();
    }

    public record TicketHeader(String posteNom, String numeroVente, String numeroTicket, BigDecimal totalTtc, BigDecimal arrondi) {
    }

    public record TicketLine(String libelle, int quantite, BigDecimal totalLigne) {
    }
}

