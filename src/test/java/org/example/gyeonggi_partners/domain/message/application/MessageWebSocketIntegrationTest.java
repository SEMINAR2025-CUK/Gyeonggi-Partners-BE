package org.example.gyeonggi_partners.domain.message.application;

import org.example.gyeonggi_partners.domain.message.api.MessageType;
import org.example.gyeonggi_partners.domain.message.api.dto.MessageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.*;

/**
 * WebSocket 메시지 전송 테스트
 * 
 * ✅ RedisSubscriber가 올바르게 WebSocket으로 메시지를 전달하는지 검증
 * - 메시지를 받으면 WebSocket을 통해 클라이언트에게 전달
 * - 올바른 destination으로 메시지 전송: "/topic/room/{roomId}"
 * - SimpMessagingTemplate의 convertAndSend 호출 검증
 * 
 * 📌 실제 WebSocket 통합 테스트가 필요하다면:
 * - @SpringBootTest와 TestRestTemplate 사용
 * - WebSocketStompClient로 실제 연결 테스트
 * - Testcontainers 사용 (Docker 필요)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("메시지 WebSocket 전송 테스트")
class MessageWebSocketIntegrationTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private RedisSubscriber redisSubscriber;

    @Test
    @DisplayName("WebSocket - Redis에서 받은 메시지를 WebSocket으로 전달")
    void testWebSocketMessageDelivery() {
        // given
        Long roomId = 123L;
        String expectedDestination = "/topic/room/123";
        MessageRequest request = createMessageRequest(roomId);

        // when - RedisSubscriber가 메시지를 받았을 때
        redisSubscriber.handleMessage(request);

        // then - WebSocket으로 메시지가 전달되었는지 검증
        verify(messagingTemplate, times(1))
            .convertAndSend(eq(expectedDestination), eq(request));
        
        System.out.println("✅ WebSocket 메시지 전달 검증 성공!");
        System.out.println("   Destination: " + expectedDestination);
        System.out.println("   메시지: " + request.getContent());
    }

    @Test
    @DisplayName("WebSocket - 여러 방에 메시지 전달")
    void testWebSocketMultipleRooms() {
        // given
        MessageRequest room1 = createMessageRequest(1L);
        MessageRequest room2 = createMessageRequest(2L);
        MessageRequest room3 = createMessageRequest(3L);

        // when
        redisSubscriber.handleMessage(room1);
        redisSubscriber.handleMessage(room2);
        redisSubscriber.handleMessage(room3);

        // then
        verify(messagingTemplate).convertAndSend("/topic/room/1", room1);
        verify(messagingTemplate).convertAndSend("/topic/room/2", room2);
        verify(messagingTemplate).convertAndSend("/topic/room/3", room3);
        verify(messagingTemplate, times(3)).convertAndSend(anyString(), any(MessageRequest.class));
        
        System.out.println("✅ 다중 방 WebSocket 전달 검증 성공!");
    }

    @Test
    @DisplayName("WebSocket - 메시지 타입별 전달 검증")
    void testWebSocketDifferentMessageTypes() {
        // given
        Long roomId = 100L;
        String destination = "/topic/room/100";
        
        MessageRequest chatMessage = createMessageRequest(roomId, MessageType.CHAT, "채팅");
        MessageRequest joinMessage = createMessageRequest(roomId, MessageType.JOIN, "입장");
        MessageRequest leaveMessage = createMessageRequest(roomId, MessageType.LEAVE, "퇴장");

        // when
        redisSubscriber.handleMessage(chatMessage);
        redisSubscriber.handleMessage(joinMessage);
        redisSubscriber.handleMessage(leaveMessage);

        // then
        verify(messagingTemplate).convertAndSend(destination, chatMessage);
        verify(messagingTemplate).convertAndSend(destination, joinMessage);
        verify(messagingTemplate).convertAndSend(destination, leaveMessage);
        verify(messagingTemplate, times(3))
            .convertAndSend(eq(destination), any(MessageRequest.class));
        
        System.out.println("✅ 메시지 타입별 WebSocket 전달 검증 성공!");
    }

    @Test
    @DisplayName("WebSocket destination 경로 형식 검증")
    void testWebSocketDestinationFormat() {
        // given
        Long roomId = 999L;
        String expectedDestination = "/topic/room/999";
        MessageRequest request = createMessageRequest(roomId);

        // when
        redisSubscriber.handleMessage(request);

        // then - destination이 "/topic/room/{roomId}" 형식인지 검증
        verify(messagingTemplate).convertAndSend(
            argThat(dest -> 
                dest.startsWith("/topic/room/") && 
                dest.equals(expectedDestination)
            ),
            eq(request)
        );
        
        System.out.println("✅ WebSocket destination 형식 검증 성공!");
        System.out.println("   규칙: /topic/room/{roomId}");
        System.out.println("   실제: " + expectedDestination);
    }

    // 테스트용 MessageRequest 생성
    private MessageRequest createMessageRequest(Long roomId) {
        return createMessageRequest(roomId, MessageType.CHAT, "테스트 메시지");
    }

    private MessageRequest createMessageRequest(Long roomId, MessageType type, String content) {
        return new MessageRequest() {
            @Override
            public MessageType getType() { return type; }
            @Override
            public String getContent() { return content; }
            @Override
            public Long getRoomId() { return roomId; }
            @Override
            public Long getUserId() { return 1L; }
        };
    }
}
