package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.core.exception.DataNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public void registerUser(final String name) {
        final String qrCodeKey = generateQrCodeKey();
        userRepository.save(User.registerUser(name, qrCodeKey));
    }

    private String generateQrCodeKey() {
        final String token = UUID.randomUUID().toString().replace("-", "");
        final String prefix = "DDD";

        return prefix + "|" + "|" + token;
    }

    @Transactional(readOnly = true)
    public User getUser(final Long id) {
        return userRepository.findById(id).orElseThrow(DataNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public boolean existsByOauthProviderAndOauthId(OAuthProvider provider, String oauthId) {
        return userRepository.existsByOauthProviderAndOauthId(provider, oauthId);
    }

    @Transactional
    public User loginOrRegisterOAuthUser(
            final OAuthProvider provider,
            final String oauthId,
            final String email,
            final String name) {
        return userRepository
                .findByOauthProviderAndOauthId(provider, oauthId)
                .orElseGet(
                        () -> {
                            User newUser = User.registerOAuthUser(provider, oauthId, email, name);
                            return userRepository.save(newUser);
                        });
    }
}
