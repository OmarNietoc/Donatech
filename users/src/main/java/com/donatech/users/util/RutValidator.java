package com.donatech.users.util;

public final class RutValidator {

    private RutValidator() {}

    /**
     * Validates a Chilean RUT using modulo-11 algorithm.
     * Accepts formats: "12345678-9", "12.345.678-9", "123456789" (no separator).
     */
    public static boolean isValid(String rut) {
        if (rut == null || rut.isBlank()) return false;

        String clean = rut.replaceAll("[.\\-]", "").toUpperCase();
        if (clean.length() < 2) return false;

        String digits = clean.substring(0, clean.length() - 1);
        char dv = clean.charAt(clean.length() - 1);

        try {
            long number = Long.parseLong(digits);
            char expected = computeDv(number);
            return dv == expected;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Normalizes RUT to format "12345678-9" */
    public static String normalize(String rut) {
        if (rut == null) return null;
        String clean = rut.replaceAll("[.\\-]", "").toUpperCase();
        if (clean.length() < 2) return rut;
        return clean.substring(0, clean.length() - 1) + "-" + clean.charAt(clean.length() - 1);
    }

    private static char computeDv(long rut) {
        int sum = 0;
        int multiplier = 2;
        while (rut > 0) {
            sum += (rut % 10) * multiplier;
            rut /= 10;
            multiplier = multiplier == 7 ? 2 : multiplier + 1;
        }
        int remainder = 11 - (sum % 11);
        if (remainder == 11) return '0';
        if (remainder == 10) return 'K';
        return (char) ('0' + remainder);
    }
}
