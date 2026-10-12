package com.ddd.manage_attendance.domain.auth.domain;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;
import org.springframework.stereotype.Component;

@Component
class InvitationCodeGenerator {
    private static final char[] CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private final RandomGenerator random;

    InvitationCodeGenerator() {
        this(new SecureRandom());
    }

    InvitationCodeGenerator(final RandomGenerator random) {
        this.random = random;
    }

    String generate() {
        final StringBuilder code = new StringBuilder(8);
        for (int index = 0; index < 8; index++) {
            code.append(CODE_CHARS[random.nextInt(CODE_CHARS.length)]);
        }
        return code.toString();
    }
}
