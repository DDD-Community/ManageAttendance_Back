package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.domain.auth.api.dto.UserInfoResponse;
import com.ddd.manage_attendance.domain.auth.api.dto.UserQrResponse;
import com.ddd.manage_attendance.domain.auth.api.dto.UserRegisterRequest;
import com.ddd.manage_attendance.domain.generation.domain.Generation;
import com.ddd.manage_attendance.domain.generation.domain.GenerationRepository;
import com.ddd.manage_attendance.domain.oauth.domain.OAuthUserInfo;
import com.ddd.manage_attendance.domain.oauth.infrastructure.common.OAuthServiceResolver;
import com.ddd.manage_attendance.domain.qr.domain.QrService;
import com.ddd.manage_attendance.domain.team.domain.Team;
import com.ddd.manage_attendance.domain.team.domain.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserFacade {
    private final UserService userService;
    private final QrService qrService;
    private final OAuthServiceResolver oauthServiceResolver;
    private final InvitationService invitationService;
    private final GenerationRepository generationRepository;
    private final TeamRepository teamRepository;
    private static final int DEFAULT_QR_SIZE = 300;

    @Transactional
    public UserInfoResponse registerUser(final UserRegisterRequest request) {
        invitationService.verifyCode(request.invitationCode());

        OAuthUserInfo oauthUserInfo =
                oauthServiceResolver.resolve(request.provider()).authenticate(request.token());

        final String qrCode = qrService.generateQrCodeKey();

        User user =
                userService.registerOAuthUser(
                        request.provider(),
                        oauthUserInfo.getSub(),
                        oauthUserInfo.getEmail(),
                        request.name(),
                        qrCode,
                        request.generationId(),
                        request.teamId(),
                        request.jobRole(),
                        request.managerRoles());

        return getUserInfo(user.getId());
    }

    @Transactional(readOnly = true)
    public UserQrResponse getUserQr(final Long id) {
        final User user = userService.getUser(id);
        final String qrString =
                qrService.generateQrBase64(user.getQrCode(), DEFAULT_QR_SIZE, DEFAULT_QR_SIZE);
        return UserQrResponse.from(user.getId(), qrString);
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(final Long userId) {
        final User user = userService.getUser(userId);

        String generationName = null;
        if (user.getGenerationId() != null) {
            generationName =
                    generationRepository
                            .findById(user.getGenerationId())
                            .map(Generation::getName)
                            .orElse(null);
        }

        String teamName = null;
        if (user.getTeamId() != null) {
            teamName = teamRepository.findById(user.getTeamId()).map(Team::getName).orElse(null);
        }

        return UserInfoResponse.from(user, generationName, teamName);
    }
}
