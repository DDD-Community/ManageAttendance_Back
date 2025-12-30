package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.domain.attendance.domain.AttendanceRepository;
import com.ddd.manage_attendance.domain.auth.api.dto.UserInfoResponse;
import com.ddd.manage_attendance.domain.auth.api.dto.UserQrResponse;
import com.ddd.manage_attendance.domain.auth.api.dto.UserRegisterRequest;
import com.ddd.manage_attendance.domain.auth.api.dto.UserUpdateRequest;
import com.ddd.manage_attendance.domain.auth.exception.GenerationMismatchException;
import com.ddd.manage_attendance.domain.auth.exception.InvalidUserRegistrationException;
import com.ddd.manage_attendance.domain.generation.domain.Generation;
import com.ddd.manage_attendance.domain.generation.domain.GenerationService;
import com.ddd.manage_attendance.domain.oauth.domain.OAuthUserInfo;
import com.ddd.manage_attendance.domain.oauth.infrastructure.common.OAuthServiceResolver;
import com.ddd.manage_attendance.domain.qr.domain.QrService;
import com.ddd.manage_attendance.domain.team.domain.Team;
import com.ddd.manage_attendance.domain.team.domain.TeamService;
import com.ddd.manage_attendance.domain.team.exception.TeamNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFacade {
    private final UserService userService;
    private final QrService qrService;
    private final OAuthServiceResolver oauthServiceResolver;
    private final InvitationService invitationService;
    private final GenerationService generationService;
    private final TeamService teamService;
    private final AttendanceRepository attendanceRepository;
    private static final int DEFAULT_QR_SIZE = 300;

    @Transactional
    public UserInfoResponse registerUser(final UserRegisterRequest request) {
        final Invitation invitation = invitationService.verifyCode(request.invitationCode());
        validateTeamAndGeneration(invitation, request.generationId(), request.teamId());

        final OAuthUserInfo oauthUserInfo =
                oauthServiceResolver.resolve(request.provider()).authenticate(request.token());

        final String qrCode = qrService.generateQrCodeKey();

        final User user =
                userService.registerOAuthUser(
                        request.provider(),
                        oauthUserInfo.getSub(),
                        oauthUserInfo.getEmail(),
                        request.name(),
                        qrCode,
                        invitation.getGeneration().getId(),
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
        final String generationName = generationService.getGenerationName(user.getGenerationId());
        final String teamName = teamService.getTeamName(user.getTeamId());

        return UserInfoResponse.from(user, generationName, teamName);
    }

    @Transactional
    public UserInfoResponse updateUserInfo(final Long userId, final UserUpdateRequest request) {
        final Invitation invitation = invitationService.verifyCode(request.invitationCode());
        validateTeamAndGeneration(invitation, request.generationId(), request.teamId());
        validateRoleRequirements(invitation, request);

        final Generation generation = invitation.getGeneration();

        userService.updateUser(
                userId,
                request.name(),
                generation.getId(),
                request.teamId(),
                request.jobRole(),
                request.managerRoles());

        return getUserInfo(userId);
    }

    private void validateTeamAndGeneration(
            final Invitation invitation, final Long requestGenerationId, final Long requestTeamId) {
        if (!invitation.getGeneration().getId().equals(requestGenerationId)) {
            throw new GenerationMismatchException();
        }
        if (requestTeamId != null) {
            Team team = teamService.findById(requestTeamId);
            if (!team.getGenerationId().equals(invitation.getGeneration().getId())) {
                throw new TeamNotFoundException();
            }
        }
    }

    private void validateRoleRequirements(
            final Invitation invitation, final UserUpdateRequest request) {
        switch (invitation.getType()) {
            case MEMBER -> {
                if (request.teamId() == null) {
                    throw new InvalidUserRegistrationException("멤버는 팀 ID가 필수입니다.");
                }
            }
            case MANAGER -> {
                if (request.managerRoles() == null || request.managerRoles().isEmpty()) {
                    throw new InvalidUserRegistrationException("운영진은 역할 목록이 필수입니다.");
                }
            }
        }
    }

    @Transactional
    public void withdrawUser(final Long userId, final String oauthToken) {
        User user = userService.getUser(userId);

        if (oauthToken != null && !oauthToken.isBlank() && user.getOauthProvider() != null) {
            oauthServiceResolver.resolve(user.getOauthProvider()).revoke(oauthToken);
        }

        attendanceRepository.deleteByUserId(userId);
        userService.deleteUser(userId);
    }
}
