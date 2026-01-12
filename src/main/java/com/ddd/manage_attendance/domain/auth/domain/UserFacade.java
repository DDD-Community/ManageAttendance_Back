package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.domain.attendance.domain.AttendanceRepository;
import com.ddd.manage_attendance.domain.auth.api.dto.UserInfoResponse;
import com.ddd.manage_attendance.domain.auth.api.dto.UserQrResponse;
import com.ddd.manage_attendance.domain.auth.api.dto.UserRegisterRequest;
import com.ddd.manage_attendance.domain.auth.api.dto.UserUpdateRequest;
import com.ddd.manage_attendance.domain.auth.exception.AlreadyRegisteredException;
import com.ddd.manage_attendance.domain.auth.exception.GenerationMismatchException;
import com.ddd.manage_attendance.domain.auth.exception.InvalidUserRegistrationException;
import com.ddd.manage_attendance.domain.generation.domain.GenerationService;
import com.ddd.manage_attendance.domain.oauth.domain.OAuthUserInfo;
import com.ddd.manage_attendance.domain.oauth.domain.dto.OAuthRevocationRequest;
import com.ddd.manage_attendance.domain.oauth.infrastructure.common.OAuthServiceResolver;
import com.ddd.manage_attendance.domain.qr.domain.QrService;
import com.ddd.manage_attendance.domain.team.domain.Team;
import com.ddd.manage_attendance.domain.team.domain.TeamService;
import com.ddd.manage_attendance.domain.team.exception.TeamNotFoundException;
import java.util.List;
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

        final UserRole userRole = UserRole.valueOf(invitation.getType().name());
        validateRoleRequirements(userRole, request.managerRoles(), request.teamId());

        final OAuthUserInfo oauthUserInfo =
                oauthServiceResolver.resolve(request.provider()).authenticate(request.token());

        userService.findByOAuthProviderAndOAuthId(request.provider(), oauthUserInfo.getSub())
                .ifPresent(user -> {
                    throw new AlreadyRegisteredException();
                });

        final String qrCode = qrService.generateQrCodeKey();

        final User user =
                userService.registerOAuthUser(
                        request.provider(),
                        oauthUserInfo.getSub(),
                        oauthUserInfo.getEmail(),
                        request.name(),
                        qrCode,
                        invitation.getGenerationId(),
                        request.teamId(),
                        request.jobRole(),
                        request.managerRoles(),
                        request.invitationCode(),
                        userRole);

        if (request.oauthRefreshToken() != null && !request.oauthRefreshToken().isBlank()) {
            user.updateOAuthRefreshToken(request.oauthRefreshToken());
        }

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

        final Long generationId = invitation.getGenerationId();
        UserRole role = UserRole.valueOf(invitation.getType().name());

        userService.updateUser(
                userId,
                request.name(),
                generationId,
                request.teamId(),
                request.jobRole(),
                request.managerRoles(),
                role);

        return getUserInfo(userId);
    }

    private void validateTeamAndGeneration(
            final Invitation invitation, final Long requestGenerationId, final Long requestTeamId) {
        if (!invitation.getGenerationId().equals(requestGenerationId)) {
            throw new GenerationMismatchException();
        }
        if (requestTeamId != null) {
            Team team = teamService.findById(requestTeamId);
            if (!team.getGenerationId().equals(invitation.getGenerationId())) {
                throw new TeamNotFoundException();
            }
        }
    }

    private void validateRoleRequirements(
            final Invitation invitation, final UserUpdateRequest request) {
        UserRole role = UserRole.valueOf(invitation.getType().name());
        validateRoleRequirements(role, request.managerRoles(), request.teamId());
    }

    private void validateRoleRequirements(
            UserRole role, List<ManagerRole> managerRoles, Long teamId) {
        switch (role) {
            case MEMBER -> {
                if (teamId == null) {
                    throw new InvalidUserRegistrationException("멤버는 팀 ID가 필수입니다.");
                }
            }
            case MANAGER -> {
                if (managerRoles == null || managerRoles.isEmpty()) {
                    throw new InvalidUserRegistrationException("운영진은 역할 목록이 필수입니다.");
                }
            }
        }
    }

    @Transactional
    public void withdrawUser(final Long userId, final String oauthToken) {
        User user = userService.getUser(userId);

        if (oauthToken != null && !oauthToken.isBlank() && user.getOauthProvider() != null) {
            try {
                OAuthRevocationRequest revocationRequest =
                        new OAuthRevocationRequest(oauthToken, user.getOauthRefreshToken());
                oauthServiceResolver.resolve(user.getOauthProvider()).revoke(revocationRequest);
            } catch (Exception e) {
                log.warn("OAuth revocation failed for user {}. Error: {}", userId, e.getMessage());
            }
        }

        attendanceRepository.deleteByUserId(userId);
        userService.deleteUser(userId);
    }




}
