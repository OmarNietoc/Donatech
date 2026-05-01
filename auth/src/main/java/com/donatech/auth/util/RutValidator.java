package com.donatech.auth.util;

public class RutValidator {

    public static boolean isValid(String rut) {
        if (rut == null || rut.isBlank()) return false;
        String normalized = rut.replaceAll("[^0-9kK]", "").toUpperCase();
        if (normalized.length() < 2) return false;

        String body = normalized.substring(0, normalized.length() - 1);
        char dv = normalized.charAt(normalized.length() - 1);

        int sum = 0;
        int factor = 2;
        for (int i = body.length() - 1; i >= 0; i--) {
            sum += Character.getNumericValue(body.charAt(i)) * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        int remainder = 11 - (sum % 11);
        char expected = remainder == 11 ? '0' : remainder == 10 ? 'K' : Character.forDigit(remainder, 10);

        return dv == expected;
    }

    public static String normalize(String rut) {
        return rut.replaceAll("[^0-9kK]", "").toUpperCase();
    }
}
