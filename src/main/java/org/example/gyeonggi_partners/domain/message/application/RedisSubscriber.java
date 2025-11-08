package org.example.gyeonggi_partners.domain.message.application;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.gyeonggi_partners.domain.message.api.dto.MessageRequest;
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
            // 발행된 메세지를 MessageRequest 객체로 역직렬화, 엔티티 대신 dto로 역직렬화 해야 함!!
            MessageRequest message=objectMapper.readValue(publishMessage,MessageRequest.class);

            // 해당 채팅방을 구독하고 있는 클라이언트에게 메세지를 보냄
            // 목적지는 /topic/room/{roomId}
            messagingTemplate.convertAndSend("/topic/room/"+message.getRoomId(),message);

        } catch (JsonMappingException e) { //json 형식이 지켜지지 않았을 때
            log.error("!!!메세지 역직렬화 실패!!!: {}", publishMessage,e);

        } catch (Exception e) {
            log.error("!!!메세지 처리 중 알 수 없는 오류 발생!!!",e);
        } // 에외를 던져버리면 redis 스레드가 죽어버려서 단순 로그만 남김
    }

}
