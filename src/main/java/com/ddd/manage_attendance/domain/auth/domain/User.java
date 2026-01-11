package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.core.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Getter
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "bigint")
    private Long id;

    @Comment("이름")
    @Column(name = "name", nullable = true, columnDefinition = "varchar(30)")
    private String name;

    @Comment("이메일")
    @Column(name = "email", nullable = true, columnDefinition = "varchar(100)")
    private String email;

    @NotNull
    @Comment("qr코드")
    @Column(unique = true, name = "qr_code", nullable = false, columnDefinition = "varchar(100)")
    private String qrCode;

    @Comment("OAuth 제공자")
    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = true, columnDefinition = "varchar(20)")
    private OAuthProvider oauthProvider;

    @Comment("OAuth 제공자 고유 ID")
    @Column(unique = true, name = "oauth_id", nullable = true, columnDefinition = "varchar(255)")
    private String oauthId;

    @Comment("OAuth 제공자의 Refresh Token (Apple 전용, 탈퇴 시 연결 해제에 사용)")
    @Column(name = "refresh_token", nullable = true, columnDefinition = "varchar(255)")
    private String oauthRefreshToken;

    @Comment("초대 코드")
    @Column(name = "invitation_code", nullable = false, columnDefinition = "varchar(50)")
    private String invitationCode;

    @Comment("유저 역할 (MEMBER/MANAGER)")
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, columnDefinition = "varchar(20)")
    private UserRole role;

    @NotNull
    @Comment("기수 Id")
    @Column(name = "generation_id", columnDefinition = "bigint")
    private Long generationId;

    @Comment("팀 Id")
    @Column(name = "team_id", nullable = true, columnDefinition = "bigint")
    private Long teamId;

    @Comment("매니저 업무 목록")
    @Convert(converter = ManagerRoleListConverter.class)
    @Column(name = "manager_roles", columnDefinition = "text")
    private List<ManagerRole> managerRoles = new ArrayList<>();

    @NotNull
    @Comment("직군")
    @Enumerated(EnumType.STRING)
    @Column(name = "job", nullable = false, columnDefinition = "varchar(10)")
    private JobRole job;

    @Builder(access = AccessLevel.PRIVATE)
    public User(
            String name,
            String email,
            String qrCode,
            OAuthProvider oauthProvider,
            String oauthId,
            Long generationId,
            Long teamId,
            JobRole job,
            List<ManagerRole> managerRoles,
            String invitationCode,
            UserRole role) {
        this.name = name;
        this.email = email;
        this.qrCode = qrCode;
        this.oauthProvider = oauthProvider;
        this.oauthId = oauthId;
        this.generationId = generationId;
        this.teamId = teamId;
        this.job = job;
        if (managerRoles != null) {
            this.managerRoles.addAll(managerRoles);
        }
        this.invitationCode = invitationCode;
        this.role = role;
    }

    public static User registerUser(
            String name,
            String qrCode,
            Long generationId,
            Long teamId,
            OAuthProvider oauthProvider,
            String oauthId,
            String email,
            JobRole job,
            List<ManagerRole> managerRoles,
            String invitationCode,
            UserRole role) {
        return User.builder()
                .name(name)
                .qrCode(qrCode)
                .generationId(generationId)
                .teamId(teamId)
                .oauthProvider(oauthProvider)
                .oauthId(oauthId)
                .job(job)
                .email(email)
                .managerRoles(managerRoles)
                .invitationCode(invitationCode)
                .role(role)
                .build();
    }

    public void updateProfile(
            String name,
            Long generationId,
            Long teamId,
            JobRole job,
            List<ManagerRole> managerRoles,
            UserRole role) {
        this.name = name;
        this.generationId = generationId;
        this.teamId = teamId;
        this.job = job;
        this.managerRoles.clear();
        if (managerRoles != null) {
            this.managerRoles.addAll(managerRoles);
        }
        this.role = role;
    }

    public void updateOAuthRefreshToken(String oauthRefreshToken) {
        this.oauthRefreshToken = oauthRefreshToken;
    }

    public List<ManagerRole> getManagerRolesOrNull() {
        if (managerRoles == null || managerRoles.isEmpty()) {
            return null;
        }
        return managerRoles;
    }

    public boolean isManager() {
        return this.role == UserRole.MANAGER;
    }

    public void validateManager() {
        if (!isManager()) {
            throw new ManagerOnlyOperationException();
        }
    }
}
