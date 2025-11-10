package org.example.gyeonggi_partners.domain.message.application;

import org.example.gyeonggi_partners.domain.message.api.MessageType;
import org.example.gyeonggi_partners.domain.message.api.dto.MessageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Redis Publisher 테스트
 * 
 * ✅ RedisPublisher가 올바르게 메시지를 발행하는지 검증
 * - ChannelTopic의 채널로 메시지 발행
 * - RedisTemplate의 convertAndSend 호출 검증
 * - 실제 Redis 서버 없이도 테스트 가능 (Mock 사용)
 * 
 * 📌 실제 프로젝트에서는:
 * - ChannelTopic이 "chatChannel"로 고정
 * - 모든 메시지가 같은 채널로 발행됨
 * - RedisSubscriber가 메시지를 받아 roomId별로 WebSocket 분기
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("메시지 Redis Publisher 테스트")
class MessageRedisIntegrationTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ChannelTopic topic;

    @InjectMocks
    private RedisPublisher redisPublisher;

    @Test
    @DisplayName("Redis Pub/Sub - 메시지 발행 검증")
    void testRedisPublish() {
        // given
        Long roomId = 123L;
        String channelName = "chatChannel";
        MessageRequest request = createMessageRequest(roomId);
        
        when(topic.getTopic()).thenReturn(channelName);

        // when
        redisPublisher.publish(request);

        // then - RedisTemplate이 올바른 채널과 메시지로 호출되었는지 검증
        verify(redisTemplate, times(1))
            .convertAndSend(eq(channelName), eq(request));
        
        System.out.println("✅ Redis 발행 검증 성공!");
        System.out.println("   채널: " + channelName);
        System.out.println("   메시지 타입: " + request.getType());
        System.out.println("   메시지 내용: " + request.getContent());
    }

    @Test
    @DisplayName("Redis Pub/Sub - 여러 메시지 발행 검증")
    void testRedisPublishMultipleMessages() {
        // given
        String channelName = "chatChannel";
        when(topic.getTopic()).thenReturn(channelName);
        
        MessageRequest msg1 = createMessageRequest(1L);
        MessageRequest msg2 = createMessageRequest(2L);
        MessageRequest msg3 = createMessageRequest(3L);

        // when
        redisPublisher.publish(msg1);
        redisPublisher.publish(msg2);
        redisPublisher.publish(msg3);

        // then - 3개의 메시지가 모두 같은 채널로 발행되었는지 검증
        verify(redisTemplate).convertAndSend(channelName, msg1);
        verify(redisTemplate).convertAndSend(channelName, msg2);
        verify(redisTemplate).convertAndSend(channelName, msg3);
        verify(redisTemplate, times(3)).convertAndSend(anyString(), any());
        
        System.out.println("✅ 다중 메시지 발행 검증 성공!");
    }

    @Test
    @DisplayName("Redis Pub/Sub - 메시지 타입별 발행 검증")
    void testRedisPublishWithDifferentMessageTypes() {
        // given
        String channelName = "chatChannel";
        when(topic.getTopic()).thenReturn(channelName);
        
        MessageRequest chatMessage = createMessageRequest(100L, MessageType.CHAT, "채팅 메시지");
        MessageRequest joinMessage = createMessageRequest(100L, MessageType.JOIN, "입장");
        MessageRequest leaveMessage = createMessageRequest(100L, MessageType.LEAVE, "퇴장");

        // when
        redisPublisher.publish(chatMessage);
        redisPublisher.publish(joinMessage);
        redisPublisher.publish(leaveMessage);

        // then
        verify(redisTemplate).convertAndSend(channelName, chatMessage);
        verify(redisTemplate).convertAndSend(channelName, joinMessage);
        verify(redisTemplate).convertAndSend(channelName, leaveMessage);
        
        System.out.println("✅ 메시지 타입별 발행 검증 성공!");
    }

    @Test
    @DisplayName("Redis 채널 사용 검증")
    void testChannelTopicUsage() {
        // given
        String channelName = "chatChannel";
        when(topic.getTopic()).thenReturn(channelName);
        
        MessageRequest request = createMessageRequest(999L);

        // when
        redisPublisher.publish(request);

        // then - ChannelTopic의 getTopic()이 호출되었는지 검증
        verify(topic, atLeastOnce()).getTopic();
        verify(redisTemplate).convertAndSend(channelName, request);
        
        System.out.println("✅ ChannelTopic 사용 검증 성공!");
        System.out.println("   채널: " + channelName);
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
