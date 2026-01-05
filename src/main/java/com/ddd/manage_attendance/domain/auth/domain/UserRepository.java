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

    @Query(value = "SELECT * FROM member", nativeQuery = true)
    List<java.util.Map<String, Object>> findAllRaw();

    @org.springframework.data.jpa.repository.Modifying
    @Query(value = "DELETE FROM member WHERE id = :id", nativeQuery = true)
    void forceDeleteById(@Param("id") Long id);

    @org.springframework.data.jpa.repository.Modifying
    @Query(value = "DELETE FROM user_manager_role WHERE user_id = :id", nativeQuery = true)
    void forceDeleteUserManagerRole(@Param("id") Long id);

    @org.springframework.data.jpa.repository.Modifying
    @Query(value = "DELETE FROM token WHERE user_id = :id", nativeQuery = true)
    void forceDeleteRefreshToken(@Param("id") Long id);
}
