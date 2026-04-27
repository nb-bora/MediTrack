package cm.pharma;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test “smoke” : vérifie que le contexte Spring démarre.
 *
 * <p>Ce test est volontairement minimal : il sert de garde-fou lors des refactorings
 * d’assemblage (configuration Spring, composants, wiring).</p>
 */
@SpringBootTest
class PharmaApplicationTests {

    @Test
    void contextLoads() {
        // Le simple chargement du contexte est le test.
    }
}

