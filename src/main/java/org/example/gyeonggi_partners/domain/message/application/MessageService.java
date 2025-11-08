package org.example.gyeonggi_partners.domain.message.application;

import lombok.RequiredArgsConstructor;
import org.example.gyeonggi_partners.common.exception.BusinessException;
import org.example.gyeonggi_partners.domain.message.api.MessageType;
import org.example.gyeonggi_partners.domain.message.api.dto.MessageRequest;
import org.example.gyeonggi_partners.domain.message.domain.model.Message;
import org.example.gyeonggi_partners.domain.message.exception.MessageErrorCode;
import org.example.gyeonggi_partners.domain.message.infra.MessageEntity;
import org.example.gyeonggi_partners.domain.user.domain.model.User;
import org.example.gyeonggi_partners.domain.user.infra.persistence.UserRepositoryImpl;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final UserRepositoryImpl userRepositoryImpl;
    private final RedisPublisher redisPublisher;


    public void processChatMessage(MessageRequest request) {
        // 메세지 타입이 chat일 경우 redis로 발행
        if (request.getType().equals(MessageType.CHAT)) {
            redisPublisher.publish(request);
        }
    }

    public void processJoinMessage(MessageRequest request, SimpMessageHeaderAccessor headerAccessor) {

        // 웹소켓 세션에 사용자 id 저장
        headerAccessor.getSessionAttributes().put("userId",request.getUserId());

        //메세지 타입이 join인 경우, redis로 발행
        if (request.getType().equals(MessageType.JOIN)) {
            redisPublisher.publish(request);
        }
    }
}
