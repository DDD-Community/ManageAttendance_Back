package com.ddd.manage_attendance.domain.auth.domain;

import com.ddd.manage_attendance.core.exception.DataNotFoundException;
import com.ddd.manage_attendance.domain.auth.api.dto.InternalInvitationItem;
import com.ddd.manage_attendance.domain.auth.exception.InvitationCodeGenerationFailedException;
import com.ddd.manage_attendance.domain.auth.exception.InvitationDuplicatedException;
import com.ddd.manage_attendance.domain.generation.domain.GenerationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InternalInvitationFacade {
    private final InvitationRepository invitations;
    private final UserRepository users;
    private final GenerationRepository generations;
    private final InvitationCodeGenerator codeGenerator;

    @Transactional(readOnly = true)
    public List<InternalInvitationItem> listInvitations(final Long generationId) {
        requireGeneration(generationId);
        return invitations.findAllByGenerationIdOrderByIdDesc(generationId).stream()
                .map(
                        invitation ->
                                new InternalInvitationItem(
                                        invitation.getId(),
                                        invitation.getCode(),
                                        invitation.getType(),
                                        invitation.getDescription(),
                                        users.countByInvitationCodeAndGenerationId(
                                                invitation.getCode(), generationId),
                                        invitation.getCreatedDate()))
                .toList();
    }

    @Transactional
    public InternalInvitationItem createInvitation(
            final Long generationId,
            final InvitationType type,
            final String requestedCode,
            final String description) {
        requireGeneration(generationId);
        final String code = requestedCode == null ? generateUniqueCode() : requestedCode;
        if (invitations.existsByCode(code)) {
            throw new InvitationDuplicatedException();
        }
        try {
            final Invitation invitation =
                    invitations.saveAndFlush(
                            Invitation.createInvitation(code, type, generationId, description));
            return new InternalInvitationItem(
                    invitation.getId(),
                    invitation.getCode(),
                    invitation.getType(),
                    invitation.getDescription(),
                    0,
                    invitation.getCreatedDate());
        } catch (DataIntegrityViolationException exception) {
            throw new InvitationDuplicatedException();
        }
    }

    @Transactional
    public void deleteInvitation(final Long id) {
        final Invitation invitation =
                invitations.findById(id).orElseThrow(DataNotFoundException::new);
        invitations.delete(invitation);
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < 5; attempt++) {
            final String code = codeGenerator.generate();
            if (!invitations.existsByCode(code)) {
                return code;
            }
        }
        throw new InvitationCodeGenerationFailedException();
    }

    private void requireGeneration(final Long id) {
        if (!generations.existsById(id)) {
            throw new DataNotFoundException();
        }
    }
}
