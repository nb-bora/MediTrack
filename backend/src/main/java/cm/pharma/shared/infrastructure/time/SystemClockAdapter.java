package cm.pharma.shared.infrastructure.time;

import cm.pharma.shared.application.ClockPort;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * Implémentation système du port {@link ClockPort}.
 */
@Component
public class SystemClockAdapter implements ClockPort {
    @Override
    public Instant now() {
        return Instant.now();
    }
}

