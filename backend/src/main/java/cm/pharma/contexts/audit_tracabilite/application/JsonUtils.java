package cm.pharma.contexts.audit_tracabilite.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * Utilitaire JSON minimal pour sérialiser les détails d’audit.
 */
final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtils() {
    }

    static String toJson(Map<String, Object> map) {
        try {
            return MAPPER.writeValueAsString(map == null ? Map.of() : map);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Sérialisation JSON impossible", e);
        }
    }
}

