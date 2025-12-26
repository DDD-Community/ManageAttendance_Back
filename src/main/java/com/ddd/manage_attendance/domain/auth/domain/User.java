package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.core.common.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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

    @NotNull
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

    @NotNull
    @Comment("기수 Id")
    @Column(name = "generation_id", columnDefinition = "bigint")
    private Long generationId;

    @Comment("팀 Id")
    @Column(name = "team_id", columnDefinition = "bigint")
    private Long teamId;

    @Comment("매니저 업무 목록")
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_manager_role", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", columnDefinition = "varchar(20)")
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
            List<ManagerRole> managerRoles) {
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
            List<ManagerRole> managerRoles) {
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
                .build();
    }
}
