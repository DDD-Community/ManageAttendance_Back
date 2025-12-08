package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.core.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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

    @Builder(access = AccessLevel.PRIVATE)
    public User(
            String name, String email, String qrCode, OAuthProvider oauthProvider, String oauthId) {
        this.name = name;
        this.email = email;
        this.qrCode = qrCode;
        this.oauthProvider = oauthProvider;
        this.oauthId = oauthId;
    }

    public static User registerUser(String name, String qrCode) {
        return User.builder().name(name).qrCode(qrCode).oauthProvider(OAuthProvider.NONE).build();
    }

    public static User registerOAuthUser(
            OAuthProvider provider, String oauthId, String email, String name) {
        return User.builder()
                .name(name)
                .email(email)
                .oauthProvider(provider)
                .oauthId(oauthId)
                .build();
    }
}
