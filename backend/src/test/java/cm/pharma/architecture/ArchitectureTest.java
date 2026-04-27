package cm.pharma.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

/**
 * Tests d’architecture (ArchUnit) pour garantir la Clean Architecture.
 *
 * <p>Ces règles sont une “barrière” automatique : si une couche commence à dépendre
 * d’une autre de façon interdite, le build échoue.</p>
 */
class ArchitectureTest {

    private static final String BASE = "cm.pharma";

    @Test
    void leDomaineNeDependDeAucunFramework() {
        JavaClasses classes = new ClassFileImporter().importPackages(BASE);

        ArchRuleDefinition.noClasses()
                .that()
                .resideInAnyPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "com.fasterxml.jackson.."
                )
                .because("Le domaine doit rester Java pur (sans Spring/JPA/Jackson)")
                .check(classes);
    }
}

