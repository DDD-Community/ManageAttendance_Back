package com.ddd.manage_attendance.domain.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ddd.manage_attendance.domain.auth.api.dto.InternalInvitationItem;
import com.ddd.manage_attendance.domain.auth.exception.InvitationCodeGenerationFailedException;
import com.ddd.manage_attendance.domain.auth.exception.InvitationDuplicatedException;
import com.ddd.manage_attendance.domain.generation.domain.GenerationRepository;
import java.util.List;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InternalInvitationFacadeTest {
    private static final Long GENERATION_ID = 13L;

    @Mock private InvitationRepository invitations;
    @Mock private UserRepository users;
    @Mock private GenerationRepository generations;
    @Mock private InvitationCodeGenerator codeGenerator;
    @Mock private RandomGenerator random;

    @Test
    @DisplayName("자동 생성한 초대 코드는 허용된 문자 8자로 구성된다")
    void createInvitation_generatesEightAllowedCharacters() {
        given(random.nextInt(anyInt())).willReturn(0);
        final InvitationCodeGenerator generator = new InvitationCodeGenerator(random);
        final InternalInvitationFacade facade =
                new InternalInvitationFacade(invitations, users, generations, generator);
        given(generations.existsById(GENERATION_ID)).willReturn(true);
        given(invitations.saveAndFlush(any(Invitation.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        final InternalInvitationItem result =
                facade.createInvitation(GENERATION_ID, InvitationType.MEMBER, null, null);

        assertThat(result.code()).hasSize(8).matches("[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{8}");
    }

    @Test
    @DisplayName("자동 생성 코드가 충돌하면 다시 생성한다")
    void createInvitation_retriesCollidingCode() {
        final InternalInvitationFacade facade = facade();
        given(generations.existsById(GENERATION_ID)).willReturn(true);
        given(codeGenerator.generate()).willReturn("ABCDEFGH", "JKLMNPQR");
        given(invitations.existsByCode("ABCDEFGH")).willReturn(true);
        final Invitation saved =
                Invitation.createInvitation(
                        "JKLMNPQR", InvitationType.MEMBER, GENERATION_ID, "멤버용");
        ReflectionTestUtils.setField(saved, "id", 1L);
        given(invitations.saveAndFlush(any(Invitation.class))).willReturn(saved);

        final InternalInvitationItem result =
                facade.createInvitation(GENERATION_ID, InvitationType.MEMBER, null, "멤버용");

        assertThat(result.code()).isEqualTo("JKLMNPQR");
        verify(codeGenerator, times(2)).generate();
    }

    @Test
    @DisplayName("자동 생성 코드가 다섯 번 충돌하면 생성 실패 예외가 발생한다")
    void createInvitation_afterFiveCollisions_throws() {
        final InternalInvitationFacade facade = facade();
        given(generations.existsById(GENERATION_ID)).willReturn(true);
        given(codeGenerator.generate()).willReturn("ABCDEFGH");
        given(invitations.existsByCode("ABCDEFGH")).willReturn(true);

        assertThatThrownBy(
                        () ->
                                facade.createInvitation(
                                        GENERATION_ID, InvitationType.MEMBER, null, null))
                .isInstanceOf(InvitationCodeGenerationFailedException.class);
        verify(codeGenerator, times(5)).generate();
        verify(invitations, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("입력한 초대 코드가 중복이면 중복 예외가 발생한다")
    void createInvitation_whenProvidedCodeDuplicated_throws() {
        final InternalInvitationFacade facade = facade();
        given(generations.existsById(GENERATION_ID)).willReturn(true);
        given(invitations.existsByCode("EXISTING")).willReturn(true);

        assertThatThrownBy(
                        () ->
                                facade.createInvitation(
                                        GENERATION_ID, InvitationType.MEMBER, "EXISTING", null))
                .isInstanceOf(InvitationDuplicatedException.class);
        verify(codeGenerator, never()).generate();
        verify(invitations, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("초대 코드 가입자 수는 같은 기수 회원만 센다")
    void listInvitations_countsRegisteredMembersWithinGeneration() {
        final InternalInvitationFacade facade = facade();
        final Invitation invitation =
                Invitation.createInvitation("REUSED", InvitationType.MEMBER, GENERATION_ID, null);
        given(generations.existsById(GENERATION_ID)).willReturn(true);
        given(invitations.findAllByGenerationIdOrderByIdDesc(GENERATION_ID))
                .willReturn(List.of(invitation));
        given(users.countByInvitationCodeAndGenerationId("REUSED", GENERATION_ID)).willReturn(3L);

        final List<InternalInvitationItem> items = facade.listInvitations(GENERATION_ID);

        assertThat(items).extracting(InternalInvitationItem::registeredCount).containsExactly(3L);
    }

    private InternalInvitationFacade facade() {
        return new InternalInvitationFacade(invitations, users, generations, codeGenerator);
    }
}
