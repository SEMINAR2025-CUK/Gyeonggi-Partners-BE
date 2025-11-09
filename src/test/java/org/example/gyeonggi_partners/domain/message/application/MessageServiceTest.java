package org.example.gyeonggi_partners.domain.message.application;

import org.example.gyeonggi_partners.common.exception.BusinessException;
import org.example.gyeonggi_partners.common.exception.MessageException;
import org.example.gyeonggi_partners.domain.discussionRoom.infra.persistence.discussionRoom.DiscussionRoomEntity;
import org.example.gyeonggi_partners.domain.discussionRoom.infra.persistence.discussionRoom.DiscussionRoomJpaRepository;
import org.example.gyeonggi_partners.domain.discussionRoom.infra.persistence.member.MemberJpaRepository;
import org.example.gyeonggi_partners.domain.message.api.MessageType;
import org.example.gyeonggi_partners.domain.message.api.dto.MessagePageResponse;
import org.example.gyeonggi_partners.domain.message.api.dto.MessageRequest;
import org.example.gyeonggi_partners.domain.message.exception.MessageErrorCode;
import org.example.gyeonggi_partners.domain.message.infra.MessageEntity;
import org.example.gyeonggi_partners.domain.message.infra.MessageRepository;
import org.example.gyeonggi_partners.domain.user.exception.UserErrorCode;
import org.example.gyeonggi_partners.domain.user.infra.persistence.UserEntity;
import org.example.gyeonggi_partners.domain.user.infra.persistence.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService 단위 테스트")
class MessageServiceTest {

    @Mock
    private RedisPublisher redisPublisher;

    @Mock
    private MemberJpaRepository memberJpaRepository;

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private DiscussionRoomJpaRepository discussionRoomJpaRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private SimpMessageHeaderAccessor headerAccessor;

    @InjectMocks
    private MessageService messageService;

    private UserEntity testUser;
    private DiscussionRoomEntity testRoom;
    private MessageRequest validMessageRequest;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .id(1L)
                .loginId("testuser")
                .loginPw("password123")
                .email("test@example.com")
                .name("테스트유저")
                .nickname("테스트닉네임")
                .phoneNumber("010-1234-5678")
                .role("USER")
                .build();

        testRoom = DiscussionRoomEntity.builder()
                .id(1L)
                .title("테스트 논의방")
                .description("테스트용 논의방입니다.")
                .region(org.example.gyeonggi_partners.domain.discussionRoom.domain.model.Region.SUWON)
                .accessLevel(org.example.gyeonggi_partners.domain.discussionRoom.domain.model.AccessLevel.PUBLIC)
                .memberCount(1)
                .build();

        validMessageRequest = createMessageRequest(MessageType.CHAT, "테스트 메시지", 1L, 1L);
    }

    @Test
    @DisplayName("정상적인 채팅 메시지 처리")
    void processChatMessage_Success() {
        // given
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("userId", 1L);
        
        given(headerAccessor.getSessionAttributes()).willReturn(sessionAttributes);
        given(memberJpaRepository.existsByUserIdAndRoomId(1L, 1L)).willReturn(true);
        given(userJpaRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(discussionRoomJpaRepository.findById(1L)).willReturn(Optional.of(testRoom));
        given(messageRepository.save(any(MessageEntity.class))).willReturn(createMessageEntity());

        // when
        messageService.processChatMessage(validMessageRequest, headerAccessor);

        // then
        then(messageRepository).should(times(1)).save(any(MessageEntity.class));
        then(redisPublisher).should(times(1)).publish(validMessageRequest);
    }

    @Test
    @DisplayName("세션 유저와 요청 유저 불일치 시 예외 발생")
    void processChatMessage_UserInconsistency() {
        // given
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("userId", 2L); // 다른 유저 ID
        
        given(headerAccessor.getSessionAttributes()).willReturn(sessionAttributes);

        // when & then
        assertThatThrownBy(() -> messageService.processChatMessage(validMessageRequest, headerAccessor))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", MessageErrorCode.MESSAGE_USER_INCOINSISTENCY);
    }

    @Test
    @DisplayName("세션에 userId가 없을 때 예외 발생")
    void processChatMessage_NoSessionUser() {
        // given
        Map<String, Object> sessionAttributes = new HashMap<>();
        
        given(headerAccessor.getSessionAttributes()).willReturn(sessionAttributes);

        // when & then
        assertThatThrownBy(() -> messageService.processChatMessage(validMessageRequest, headerAccessor))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", MessageErrorCode.MESSAGE_USER_INCOINSISTENCY);
    }

    @Test
    @DisplayName("빈 메시지 내용으로 예외 발생")
    void processChatMessage_EmptyContent() {
        // given
        MessageRequest emptyRequest = createMessageRequest(MessageType.CHAT, "", 1L, 1L);
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("userId", 1L);
        
        given(headerAccessor.getSessionAttributes()).willReturn(sessionAttributes);

        // when & then
        assertThatThrownBy(() -> messageService.processChatMessage(emptyRequest, headerAccessor))
                .isInstanceOf(MessageException.class)
                .hasFieldOrPropertyWithValue("errorCode", MessageErrorCode.MESSAGE_CONTENT_EMPTY);
    }

    @Test
    @DisplayName("공백만 있는 메시지 내용으로 예외 발생")
    void processChatMessage_BlankContent() {
        // given
        MessageRequest blankRequest = createMessageRequest(MessageType.CHAT, "   ", 1L, 1L);
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("userId", 1L);
        
        given(headerAccessor.getSessionAttributes()).willReturn(sessionAttributes);

        // when & then
        assertThatThrownBy(() -> messageService.processChatMessage(blankRequest, headerAccessor))
                .isInstanceOf(MessageException.class)
                .hasFieldOrPropertyWithValue("errorCode", MessageErrorCode.MESSAGE_CONTENT_EMPTY);
    }

    @Test
    @DisplayName("2000자를 초과하는 메시지로 예외 발생")
    void processChatMessage_TooLongContent() {
        // given
        String longContent = "a".repeat(2001);
        MessageRequest longRequest = createMessageRequest(MessageType.CHAT, longContent, 1L, 1L);
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("userId", 1L);
        
        given(headerAccessor.getSessionAttributes()).willReturn(sessionAttributes);

        // when & then
        assertThatThrownBy(() -> messageService.processChatMessage(longRequest, headerAccessor))
                .isInstanceOf(MessageException.class)
                .hasFieldOrPropertyWithValue("errorCode", MessageErrorCode.MESSAGE_TOO_LONG);
    }

    @Test
    @DisplayName("논의방 멤버가 아닐 때 예외 발생")
    void processChatMessage_NotMember() {
        // given
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("userId", 1L);
        
        given(headerAccessor.getSessionAttributes()).willReturn(sessionAttributes);
        given(memberJpaRepository.existsByUserIdAndRoomId(1L, 1L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> messageService.processChatMessage(validMessageRequest, headerAccessor))
                .isInstanceOf(MessageException.class);
    }

    @Test
    @DisplayName("존재하지 않는 유저로 예외 발생")
    void processChatMessage_UserNotFound() {
        // given
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("userId", 1L);
        
        given(headerAccessor.getSessionAttributes()).willReturn(sessionAttributes);
        given(memberJpaRepository.existsByUserIdAndRoomId(1L, 1L)).willReturn(true);
        given(userJpaRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> messageService.processChatMessage(validMessageRequest, headerAccessor))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 논의방으로 예외 발생")
    void processChatMessage_RoomNotFound() {
        // given
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("userId", 1L);
        
        given(headerAccessor.getSessionAttributes()).willReturn(sessionAttributes);
        given(memberJpaRepository.existsByUserIdAndRoomId(1L, 1L)).willReturn(true);
        given(userJpaRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(discussionRoomJpaRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> messageService.processChatMessage(validMessageRequest, headerAccessor))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("JOIN 타입 메시지 처리")
    void processJoinMessage_Success() {
        // given
        MessageRequest joinRequest = createMessageRequest(MessageType.JOIN, "사용자가 입장했습니다.", 1L, 1L);
        Map<String, Object> sessionAttributes = new HashMap<>();
        
        given(headerAccessor.getSessionAttributes()).willReturn(sessionAttributes);

        // when
        messageService.processJoinMessage(joinRequest, headerAccessor);

        // then
        assertThat(sessionAttributes).containsEntry("userId", 1L);
        then(redisPublisher).should(times(1)).publish(joinRequest);
    }

    @Test
    @DisplayName("메시지 목록 조회 - 커서 없이 최신 메시지 조회")
    void getMessages_WithoutCursor() {
        // given
        Long roomId = 1L;
        int size = 30;
        List<MessageEntity> mockMessages = createMockMessageList(20);
        
        given(messageRepository.findLatesetMessages(eq(roomId), any(PageRequest.class)))
                .willReturn(mockMessages);

        // when
        MessagePageResponse response = messageService.getMessages(roomId, null, size);

        // then
        assertThat(response.getMessages()).hasSize(20);
        assertThat(response.isHasNext()).isFalse();
        then(messageRepository).should(times(1)).findLatesetMessages(eq(roomId), any(PageRequest.class));
        then(messageRepository).should(never()).findMessagesBeforeCursor(anyLong(), anyLong(), any(PageRequest.class));
    }

    @Test
    @DisplayName("메시지 목록 조회 - 커서 기반 페이징")
    void getMessages_WithCursor() {
        // given
        Long roomId = 1L;
        Long cursor = 100L;
        int size = 30;
        List<MessageEntity> mockMessages = createMockMessageList(31); // size+1
        
        given(messageRepository.findMessagesBeforeCursor(eq(roomId), eq(cursor), any(PageRequest.class)))
                .willReturn(mockMessages);

        // when
        MessagePageResponse response = messageService.getMessages(roomId, cursor, size);

        // then
        assertThat(response.getMessages()).hasSize(30); // size만큼만
        assertThat(response.isHasNext()).isTrue(); // 다음 페이지 존재
        then(messageRepository).should(times(1)).findMessagesBeforeCursor(eq(roomId), eq(cursor), any(PageRequest.class));
        then(messageRepository).should(never()).findLatesetMessages(anyLong(), any(PageRequest.class));
    }

    @Test
    @DisplayName("메시지 목록 조회 - 다음 페이지 없음")
    void getMessages_NoNextPage() {
        // given
        Long roomId = 1L;
        int size = 30;
        List<MessageEntity> mockMessages = createMockMessageList(15); // size보다 적음
        
        given(messageRepository.findLatesetMessages(eq(roomId), any(PageRequest.class)))
                .willReturn(mockMessages);

        // when
        MessagePageResponse response = messageService.getMessages(roomId, null, size);

        // then
        assertThat(response.getMessages()).hasSize(15);
        assertThat(response.isHasNext()).isFalse();
    }

    // Helper Methods
    private MessageRequest createMessageRequest(MessageType type, String content, Long roomId, Long userId) {
        return new MessageRequest() {
            @Override
            public MessageType getType() { return type; }
            @Override
            public String getContent() { return content; }
            @Override
            public Long getRoomId() { return roomId; }
            @Override
            public Long getUserId() { return userId; }
        };
    }

    private MessageEntity createMessageEntity() {
        return MessageEntity.builder()
                .id(1L)
                .content("테스트 메시지")
                .user(testUser)
                .discussionRoom(testRoom)
                .build();
    }

    private List<MessageEntity> createMockMessageList(int count) {
        List<MessageEntity> messages = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            messages.add(MessageEntity.builder()
                    .id((long) i)
                    .content("메시지 " + i)
                    .user(testUser)
                    .discussionRoom(testRoom)
                    .build());
        }
        return messages;
    }
}
