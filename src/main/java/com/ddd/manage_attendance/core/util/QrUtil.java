package com.ddd.manage_attendance.core.util;

import java.util.Base64;

public class QrUtil {
    public static String toBase64(byte[] imageBytes) {
        return Base64.getEncoder().encodeToString(imageBytes);
    }
}
