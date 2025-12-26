package com.ddd.manage_attendance.domain.auth.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByQrCode(String qrCode);

    Optional<User> findByOauthProviderAndOauthId(OAuthProvider provider, String oauthId);

    List<User> findAllByTeamId(Long teamId);
}
