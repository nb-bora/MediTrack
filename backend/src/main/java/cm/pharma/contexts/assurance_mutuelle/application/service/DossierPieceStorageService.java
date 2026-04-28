package cm.pharma.contexts.assurance_mutuelle.application.service;

import cm.pharma.contexts.assurance_mutuelle.bootstrap.TiersPayantStockageProperties;
import cm.pharma.shared.domain.BusinessRuleViolationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DossierPieceStorageService {

    private final TiersPayantStockageProperties props;

    public DossierPieceStorageService(TiersPayantStockageProperties props) {
        this.props = Objects.requireNonNull(props);
    }

    public StoredPiece store(UUID organisationId, UUID dossierId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleViolationException("Fichier requis");
        }
        String original = file.getOriginalFilename() == null ? "piece" : file.getOriginalFilename();
        String safeName = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        UUID pieceId = UUID.randomUUID();

        Path base = Path.of(props.baseDir() == null ? "./data/dossiers_tiers_payant" : props.baseDir());
        Path dir = base.resolve(organisationId.toString()).resolve(dossierId.toString());
        Path dest = dir.resolve(pieceId + "_" + safeName);

        try {
            Files.createDirectories(dir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new BusinessRuleViolationException("Impossible de stocker le fichier dossier tiers payant");
        }

        String storageKey = organisationId + "/" + dossierId + "/" + dest.getFileName();
        return new StoredPiece(pieceId, safeName, file.getContentType(), storageKey);
    }

    public record StoredPiece(UUID pieceId, String fichierNom, String contenuType, String storageKey) {
    }

    public Resource loadAsResource(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new BusinessRuleViolationException("storage_key requis");
        }
        Path base = Path.of(props.baseDir() == null ? "./data/dossiers_tiers_payant" : props.baseDir());
        Path file = base.resolve(storageKey).normalize();
        if (!file.startsWith(base.normalize())) {
            throw new BusinessRuleViolationException("storage_key invalide");
        }
        try {
            Resource r = new UrlResource(file.toUri());
            if (!r.exists()) {
                throw new BusinessRuleViolationException("Fichier introuvable");
            }
            return r;
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Impossible de lire le fichier");
        }
    }
}

