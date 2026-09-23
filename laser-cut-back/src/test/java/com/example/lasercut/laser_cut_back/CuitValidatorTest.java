package com.example.lasercut.laser_cut_back;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.example.lasercut.laser_cut_back.shared.util.CuitValidator;

public class CuitValidatorTest {

    @Test
    void testValidCuits() {
        // CUITs matemáticamente válidos (algoritmo módulo 11 de AFIP)
        assertTrue(CuitValidator.isValidCuit("20-12345678-6"));
        assertTrue(CuitValidator.isValidCuit("20123456786"));
        assertTrue(CuitValidator.isValidCuit("20-30000000-3"));
        assertTrue(CuitValidator.isValidCuit("27-30000000-8"));
        assertTrue(CuitValidator.isValidCuit("30-50000000-3"));
    }

    @Test
    void testInvalidCuits() {
        // Dígito verificador incorrecto
        assertFalse(CuitValidator.isValidCuit("20-12345678-9"));
        assertFalse(CuitValidator.isValidCuit("30-71111111-9"));

        // Longitud incorrecta
        assertFalse(CuitValidator.isValidCuit("12345678"));
        assertFalse(CuitValidator.isValidCuit("201234567890"));

        // Nulos y vacíos
        assertFalse(CuitValidator.isValidCuit(null));
        assertFalse(CuitValidator.isValidCuit(""));

        // Prefijo no válido en Argentina
        assertFalse(CuitValidator.isValidCuit("11-12345678-9"));
    }

    @Test
    void testCleanCuit() {
        assertEquals("20123456786", CuitValidator.cleanCuit("20-12345678-6"));
        assertEquals("30711111112", CuitValidator.cleanCuit("30.711.111.112"));
        assertNull(CuitValidator.cleanCuit(null));
    }

}
