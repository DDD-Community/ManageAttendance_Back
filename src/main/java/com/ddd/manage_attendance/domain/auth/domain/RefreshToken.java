package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.core.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(
        name = "refresh_token",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_refresh_token_token",
                    columnNames = {"token"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint")
    private Long id;

    @NotNull
    @Comment("사용자 ID")
    @Column(name = "user_id", nullable = false, columnDefinition = "bigint")
    private Long userId;

    @NotNull
    @Comment("Refresh Token 값")
    @Column(name = "token", nullable = false, columnDefinition = "varchar(500)")
    private String token;

    @NotNull
    @Comment("만료 시각")
    @Column(name = "expires_at", nullable = false, columnDefinition = "datetime")
    private LocalDateTime expiresAt;

    @Builder
    public RefreshToken(Long userId, String token, LocalDateTime expiresAt) {
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
