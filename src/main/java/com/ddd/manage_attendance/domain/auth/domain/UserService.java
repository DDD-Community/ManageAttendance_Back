package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.core.exception.DataNotFoundException;
import com.ddd.manage_attendance.domain.oauth.domain.dto.OAuthLoginResult;
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

    @Transactional
    public OAuthLoginResult loginOrRegisterOAuthUser(
            final OAuthProvider provider,
            final String oauthId,
            final String email,
            final String name,
            final String qrCode,
            final Long generationId,
            final Long teamId) {
        Optional<User> existingUser =
                userRepository.findByOauthProviderAndOauthId(provider, oauthId);

        if (existingUser.isPresent()) {
            return new OAuthLoginResult(existingUser.get(), false);
        }

        User newUser =
                User.registerUser(name, qrCode, generationId, teamId, provider, oauthId, email);
        return new OAuthLoginResult(userRepository.save(newUser), true);
    }
}
