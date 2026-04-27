package cm.pharma.contexts.stocks_tracabilite.interfaces.rest;

import cm.pharma.contexts.stocks_tracabilite.application.command.ReceptionnerStockCommand;
import cm.pharma.contexts.stocks_tracabilite.application.command.ReceptionnerStockUseCase;
import cm.pharma.contexts.stocks_tracabilite.application.command.TransfererStockCommand;
import cm.pharma.contexts.stocks_tracabilite.application.command.TransfererStockUseCase;
import cm.pharma.shared.interfaces.rest.OrganisationContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final ReceptionnerStockUseCase receptionnerStock;
    private final TransfererStockUseCase transfererStock;

    public StockController(ReceptionnerStockUseCase receptionnerStock, TransfererStockUseCase transfererStock) {
        this.receptionnerStock = receptionnerStock;
        this.transfererStock = transfererStock;
    }

    @PostMapping("/receptions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MAGASINIER')")
    public ReceptionResponse receptionner(@Valid @RequestBody ReceptionRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        UUID lotId = receptionnerStock.execute(new ReceptionnerStockCommand(
                orgId,
                req.produitId(),
                req.emplacementDestinationId(),
                req.numeroLot(),
                req.datePeremption(),
                req.quantite(),
                req.prixAchatUnitaire(),
                req.quantiteCommandee(),
                req.prixAttenduUnitaire(),
                req.prixFactureUnitaire(),
                req.temperatureTransportC(),
                req.confirmerPeremptionProche(),
                req.referenceDocument(),
                req.motif(),
                userId
        ));
        return new ReceptionResponse(lotId);
    }

    @PostMapping("/transferts")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','MAGASINIER')")
    public void transferer(@Valid @RequestBody TransfertRequest req, JwtAuthenticationToken auth) {
        UUID orgId = OrganisationContext.organisationId(auth);
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        transfererStock.execute(new TransfererStockCommand(
                orgId,
                req.produitId(),
                req.emplacementSourceId(),
                req.emplacementDestinationId(),
                req.quantite(),
                req.referenceDocument(),
                req.motif(),
                userId
        ));
    }

    public record ReceptionRequest(
            @NotNull UUID produitId,
            @NotNull UUID emplacementDestinationId,
            @NotBlank String numeroLot,
            @NotNull LocalDate datePeremption,
            @Min(1) int quantite,
            BigDecimal prixAchatUnitaire,
            Integer quantiteCommandee,
            BigDecimal prixAttenduUnitaire,
            BigDecimal prixFactureUnitaire,
            Double temperatureTransportC,
            boolean confirmerPeremptionProche,
            String referenceDocument,
            String motif
    ) {
    }

    public record ReceptionResponse(UUID lotId) {
    }

    public record TransfertRequest(
            @NotNull UUID produitId,
            @NotNull UUID emplacementSourceId,
            @NotNull UUID emplacementDestinationId,
            @Min(1) int quantite,
            String referenceDocument,
            String motif
    ) {
    }
}

