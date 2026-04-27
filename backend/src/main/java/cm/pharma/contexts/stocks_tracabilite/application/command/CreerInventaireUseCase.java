package cm.pharma.contexts.stocks_tracabilite.application.command;

import cm.pharma.contexts.audit_tracabilite.application.AuditWriter;
import cm.pharma.contexts.audit_tracabilite.application.AuditWriter.AuditEvent;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.InventaireJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.InventaireJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.InventaireLigneJpaEntity;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.InventaireLigneJpaRepository;
import cm.pharma.contexts.stocks_tracabilite.infrastructure.persistence.jpa.StockEmplacementJpaRepository;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreerInventaireUseCase {

    private final InventaireJpaRepository inventaires;
    private final InventaireLigneJpaRepository lignes;
    private final StockEmplacementJpaRepository stock;
    private final AuditWriter auditWriter;

    public CreerInventaireUseCase(
            InventaireJpaRepository inventaires,
            InventaireLigneJpaRepository lignes,
            StockEmplacementJpaRepository stock,
            AuditWriter auditWriter
    ) {
        this.inventaires = Objects.requireNonNull(inventaires);
        this.lignes = Objects.requireNonNull(lignes);
        this.stock = Objects.requireNonNull(stock);
        this.auditWriter = Objects.requireNonNull(auditWriter);
    }

    @Transactional
    public UUID execute(UUID organisationId, UUID createdBy) {
        inventaires.findOuvert(organisationId).ifPresent(i -> {
            throw new BusinessRuleViolationException("Un inventaire est déjà ouvert");
        });

        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        inventaires.save(InventaireJpaEntity.create(id, organisationId, createdBy, now));

        for (StockEmplacementJpaRepository.StockTheoriqueRow row : stock.computeStockTheorique(organisationId)) {
            lignes.save(InventaireLigneJpaEntity.create(
                    UUID.randomUUID(),
                    id,
                    organisationId,
                    row.getProduitId(),
                    row.getEmplacementId(),
                    row.getQuantite() == null ? 0 : row.getQuantite(),
                    now
            ));
        }

        auditWriter.write(AuditEvent.simple(
                organisationId, now, null, null, null,
                null, null, "INVENTAIRE_CREE", "Inventaire", id.toString(), null,
                Map.of()
        ));

        return id;
    }
}

