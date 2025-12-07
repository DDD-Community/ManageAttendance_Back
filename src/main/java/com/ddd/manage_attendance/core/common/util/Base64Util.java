package com.ddd.manage_attendance.core.common.util;

import java.math.BigInteger;
import java.util.Base64;

public final class Base64Util {
    private Base64Util() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static String decodeBase64UrlToString(String encoded) {
        return new String(Base64.getUrlDecoder().decode(encoded));
    }

    public static BigInteger decodeBase64UrlToBigInteger(String base64String) {
        byte[] bytes = Base64.getUrlDecoder().decode(base64String);
        return new BigInteger(1, bytes);
    }
}
