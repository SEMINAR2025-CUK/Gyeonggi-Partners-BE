package org.example.gyeonggi_partners.domain.message.application;

import lombok.RequiredArgsConstructor;
import org.example.gyeonggi_partners.common.exception.BusinessException;
import org.example.gyeonggi_partners.domain.message.api.MessageType;
import org.example.gyeonggi_partners.domain.message.api.dto.MessageRequest;
import org.example.gyeonggi_partners.domain.message.infra.MessageEntity;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisPublisher {

    // 여기서 MessageController가 아니라 메세지를 브로드캐스팅함.
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic topic;

    public void publish(MessageRequest request) {

        //ChannelTopic으로 설정된 채널에 메세지를 발행
        // convertAndSend는 주어진 객체를 직렬화기로 변환 후 지정한 토픽에 발행
        redisTemplate.convertAndSend(topic.getTopic(), request);
    }


}
