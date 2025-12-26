package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.core.exception.DataNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUser(final Long id) {
        return userRepository.findById(id).orElseThrow(DataNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public User getUserByQrCode(final String qrCode) {
        return userRepository.findByQrCode(qrCode).orElseThrow(DataNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByOAuthProviderAndOAuthId(
            final OAuthProvider provider, final String oauthId) {
        return userRepository.findByOauthProviderAndOauthId(provider, oauthId);
    }

    @Transactional(readOnly = true)
    public List<User> findUsersByTeamId(final Long teamId) {
        return userRepository.findAllByTeamId(teamId);
    }

    @Transactional
    public User registerUser(
            final String name,
            final String qrCode,
            final Long generationId,
            final Long teamId,
            final JobRole jobRole,
            final List<ManagerRole> managerRoles) {
        final String userName = name != null && !name.trim().isEmpty() ? name.trim() : "User";
        return userRepository.save(
                User.registerUser(
                        userName,
                        qrCode,
                        generationId,
                        teamId,
                        OAuthProvider.NONE,
                        null,
                        null,
                        jobRole,
                        managerRoles));
    }

    @Transactional
    public User registerOAuthUser(
            final OAuthProvider provider,
            final String oauthId,
            final String email,
            final String name,
            final String qrCode,
            final Long generationId,
            final Long teamId,
            final JobRole jobRole,
            final List<ManagerRole> managerRoles) {
        User newUser =
                User.registerUser(
                        name,
                        qrCode,
                        generationId,
                        teamId,
                        provider,
                        oauthId,
                        email,
                        jobRole,
                        managerRoles);
        return userRepository.save(newUser);
    }
}
