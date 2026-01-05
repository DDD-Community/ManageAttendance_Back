package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.domain.generation.domain.Generation;
import com.ddd.manage_attendance.domain.generation.domain.GenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final GenerationService generationService;

    public Invitation verifyCode(String code) {
        return invitationRepository
                .findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 코드입니다."));
    }

    @Transactional
    public Long createInvitation(
            String code, InvitationType type, Long generationId, String description) {
        Generation generation = generationService.findById(generationId);
        Invitation invitation =
                Invitation.createInvitation(code, type, generation.getId(), description);
        return invitationRepository.save(invitation).getId();
    }
}
