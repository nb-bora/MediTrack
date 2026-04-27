package cm.pharma.shared.interfaces.error;

import java.time.Instant;
import java.util.Map;

/**
 * Format d’erreur API stable et explicite.
 *
 * <p>Objectifs :
 * <ul>
 *   <li>Fournir une réponse exploitable par l’interface desktop (Tauri/React).</li>
 *   <li>Éviter l’ambiguïté (codes, messages, champs invalides).</li>
 *   <li>Inclure un {@code traceId} pour recouper les logs/audit.</li>
 * </ul>
 * </p>
 */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String traceId,
        Map<String, String> fieldErrors
) {
}

