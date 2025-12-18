package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.domain.auth.api.dto.UserQrResponse;
import com.ddd.manage_attendance.domain.qr.domain.QrService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserFacade {
    private final UserService userService;
    private final QrService qrService;
    private static final int DEFAULT_QR_SIZE = 300;

    @Transactional
    public void registerUser(
            final String name, final Long generationId, final Long teamId, final JobRole jobRole) {
        final String qrCode = qrService.generateQrCodeKey();
        userService.registerUser(name, qrCode, generationId, teamId, jobRole);
    }

    @Transactional(readOnly = true)
    public UserQrResponse getUserQr(final Long id) {
        final User user = userService.getUser(id);
        final String qrString =
                qrService.generateQrBase64(user.getQrCode(), DEFAULT_QR_SIZE, DEFAULT_QR_SIZE);
        return UserQrResponse.from(user.getId(), qrString);
    }
}
