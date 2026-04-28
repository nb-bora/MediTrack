package cm.pharma.contexts.ordonnances_dossier_patient.application.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AllergieDetectorTest {

    @Test
    void penicillines_bloquent_amoxicilline() {
        assertTrue(AllergieDetector.isBlocant("Pénicillines", "Amoxicilline 500mg", "Amoxicilline"));
        assertFalse(AllergieDetector.isBlocant("Pénicillines", "Paracétamol 500mg", "Paracétamol"));
    }
}

