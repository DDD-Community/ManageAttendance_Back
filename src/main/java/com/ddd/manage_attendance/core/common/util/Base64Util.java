package com.ddd.manage_attendance.core.common.util;

import java.math.BigInteger;
import java.util.Base64;

public final class Base64Util {
    private Base64Util() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static String decodeBase64UrlToString(String encoded) {
        String paddedEncoded = addPadding(encoded);
        return new String(Base64.getUrlDecoder().decode(paddedEncoded));
    }

    public static BigInteger decodeBase64UrlToBigInteger(String base64String) {
        String paddedEncoded = addPadding(base64String);
        byte[] bytes = Base64.getUrlDecoder().decode(paddedEncoded);
        return new BigInteger(1, bytes);
    }

    private static String addPadding(String base64) {
        int paddingCount = (4 - (base64.length() % 4)) % 4;
        return base64 + "=".repeat(paddingCount);
    }
}
