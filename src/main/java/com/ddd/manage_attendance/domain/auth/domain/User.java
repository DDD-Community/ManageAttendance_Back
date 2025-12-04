package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.core.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    @Column(name = "name", nullable = false, columnDefinition = "varchar(30)")
    private String name;

    @NotNull
    @Comment("이름")
    @Column(unique = true, name = "qr_code", nullable = false, columnDefinition = "varchar(100)")
    private String qrCode;

    @Builder(access = AccessLevel.PRIVATE)
    public User(String name, String qrCode) {
        this.name = name;
        this.qrCode = qrCode;
    }

    public static User registerUser(String name, String qrCode) {
        return User.builder().name(name).qrCode(qrCode).build();
    }
}
