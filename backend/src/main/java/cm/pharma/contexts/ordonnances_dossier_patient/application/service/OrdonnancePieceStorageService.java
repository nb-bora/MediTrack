package cm.pharma.contexts.ordonnances_dossier_patient.application.service;

import cm.pharma.contexts.ordonnances_dossier_patient.bootstrap.OrdonnancesStockageProperties;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OrdonnancePieceStorageService {

    private final OrdonnancesStockageProperties props;

    public OrdonnancePieceStorageService(OrdonnancesStockageProperties props) {
        this.props = Objects.requireNonNull(props);
    }

    public StoredPiece store(UUID organisationId, UUID ordonnanceId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleViolationException("Fichier requis");
        }
        String original = file.getOriginalFilename() == null ? "ordonnance" : file.getOriginalFilename();
        String safeName = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        UUID pieceId = UUID.randomUUID();

        Path base = Path.of(props.baseDir() == null ? "./data/ordonnances" : props.baseDir());
        Path dir = base.resolve(organisationId.toString()).resolve(ordonnanceId.toString());
        Path dest = dir.resolve(pieceId + "_" + safeName);

        try {
            Files.createDirectories(dir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new BusinessRuleViolationException("Impossible de stocker le fichier ordonnance");
        }

        String storageKey = organisationId + "/" + ordonnanceId + "/" + dest.getFileName();
        return new StoredPiece(pieceId, safeName, file.getContentType(), storageKey);
    }

    public record StoredPiece(UUID pieceId, String fichierNom, String contenuType, String storageKey) {
    }
}

