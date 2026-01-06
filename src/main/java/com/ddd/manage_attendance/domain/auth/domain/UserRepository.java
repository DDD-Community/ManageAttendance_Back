package com.ddd.manage_attendance.domain.auth.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByQrCode(String qrCode);

    @Query("SELECT u FROM User u WHERE u.oauthProvider = :oauthProvider AND u.oauthId = :oauthId")
    Optional<User> findByOauthProviderAndOauthId(
            @Param("oauthProvider") OAuthProvider oauthProvider, @Param("oauthId") String oauthId);

    List<User> findAllByTeamId(Long teamId);


}
