package cm.pharma.contexts.referentiel.application.service;

import cm.pharma.contexts.referentiel.infrastructure.persistence.jpa.SequenceNumerotationJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NumerotationService {
    private static final Pattern SEQ_PATTERN = Pattern.compile("\\{SEQ(\\d+)\\}");

    private final SequenceNumerotationJpaRepository sequences;

    public NumerotationService(SequenceNumerotationJpaRepository sequences) {
        this.sequences = Objects.requireNonNull(sequences);
    }

    @Transactional
    public String nextNumero(UUID organisationId, String typeDocument) {
        var seq = sequences.findByOrganisationIdAndTypeDocument(organisationId, typeDocument)
                .orElseThrow(() -> new BusinessRuleViolationException("Séquence de numérotation manquante: " + typeDocument));

        Instant now = Instant.now();
        int compteur = seq.nextAndIncrement(now);
        String format = seq.getFormat();

        LocalDate today = LocalDate.now();
        String aa = String.format("%02d", today.getYear() % 100);
        String aaaa = String.valueOf(today.getYear());

        String rendered = format
                .replace("{AA}", aa)
                .replace("{AAAA}", aaaa)
                .replace("{MM}", String.format("%02d", today.getMonthValue()));

        // Support minimal : {SEQn} => séquence padding n
        Matcher matcher = SEQ_PATTERN.matcher(rendered);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            int n = Integer.parseInt(matcher.group(1));
            matcher.appendReplacement(out, padZeros(n, compteur));
        }
        matcher.appendTail(out);
        rendered = out.toString();

        // Compatibilité historique: certains formats utilisent {SEQ4} etc. déjà couvert.
        return rendered;
    }

    private static String padZeros(int width, int value) {
        String s = Integer.toString(value);
        if (s.length() >= width) {
            return s;
        }
        return "0".repeat(width - s.length()) + s;
    }
}

