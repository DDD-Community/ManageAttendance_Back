package com.ddd.manage_attendance.domain.oauth.infrastructure.common;

public final class JWKConstants {
    public static final String KEYS = "keys";
    public static final String KID = "kid";
    public static final String KTY = "kty";
    public static final String MODULUS = "n";
    public static final String EXPONENT = "e";
    public static final String X = "x";
    public static final String Y = "y";
    public static final String CRV = "crv";

    public static final String RSA_ALGORITHM = "RSA";
    public static final String EC_ALGORITHM = "EC";
    public static final String EC_CURVE_P256 = "P-256";
    public static final String EC_CURVE_SECP256R1 = "secp256r1";

    private JWKConstants() {}
}

