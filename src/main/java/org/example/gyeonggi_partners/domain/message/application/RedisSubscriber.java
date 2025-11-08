package org.example.gyeonggi_partners.domain.message.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.gyeonggi_partners.common.exception.BusinessException;
import org.example.gyeonggi_partners.domain.message.exception.MessageErrorCode;
import org.example.gyeonggi_partners.domain.message.infra.MessageEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    // redis에서 메세지를 발행하면, Listener가 해당 메세지를 webSocket 클라이언트로 브로드캐스팅함
    public void handleMessage(String publishMessage) {
        try {
            // 발행된 메세지를 MessageEntity 객체로 역직렬화
            MessageEntity message=objectMapper.readValue(publishMessage,MessageEntity.class);

            // 해당 채팅방을 구독하고 있는 클라이언트에게 메세지를 보냄
            // 목적지는 /topic/room/{roomId}
            messagingTemplate.convertAndSend("/topic/room"+message.getRoomId(),message);

        } catch (JsonMappingException e) { //json 형식이 지켜지지 않았을 때
            throw new BusinessException(MessageErrorCode.INVALID_REQUEST_DATA_FORMAT);

        } catch (Exception e) {
            throw new BusinessException(MessageErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

}
