package com.example.lasercut.laser_cut_back.shared.util;

/**
 * Utilidad para validación de CUIT / CUIL argentino según el algoritmo Módulo 11 de AFIP/ARCA.
 */
public class CuitValidator {

    private static final int[] MULTIPLIERS = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    /**
     * Valida si un CUIT/CUIL es matemáticamente válido.
     * Acepta con o sin guiones (ej: "20-12345678-9" o "20123456789").
     */
    public static boolean isValidCuit(String cuit) {
        if (cuit == null) {
            return false;
        }

        String cleanCuit = cuit.replaceAll("[^0-9]", "");
        if (cleanCuit.length() != 11) {
            return false;
        }

        // Prefijos comunes en Argentina: 20, 23, 24, 27 (personas físicas), 30, 33, 34 (personas jurídicas)
        String prefix = cleanCuit.substring(0, 2);
        if (!prefix.matches("^(20|23|24|27|30|33|34)$")) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 10; i++) {
            int digit = Character.getNumericValue(cleanCuit.charAt(i));
            sum += digit * MULTIPLIERS[i];
        }

        int mod = 11 - (sum % 11);
        int expectedVerifier;
        if (mod == 11) {
            expectedVerifier = 0;
        } else if (mod == 10) {
            expectedVerifier = 9;
        } else {
            expectedVerifier = mod;
        }

        int actualVerifier = Character.getNumericValue(cleanCuit.charAt(10));
        return actualVerifier == expectedVerifier;
    }

    /**
     * Limpia un CUIT dejando únicamente los dígitos.
     */
    public static String cleanCuit(String cuit) {
        if (cuit == null) return null;
        return cuit.replaceAll("[^0-9]", "");
    }

}
