package org.example.gyeonggi_partners.domain.message.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.gyeonggi_partners.domain.message.api.dto.MessageRequest;
import org.example.gyeonggi_partners.domain.message.application.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@Tag(name = "Chat", description = "채팅 관련 API")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // 컨트롤러가 직접 메세지를 브로드캐스팅한다면, 다른 서버에서의 사용자들은 메세지를 받을 수 없음
    // redis를 도입하여 다중 서버 환경을 전제하였으므로, 컨트롤러에서는 클라이언트에게 수신받은 메세지를 다음 계층으로 전달하는 역할만 수행
    // 실제 브로드캐스팅 로직은 redis subscriber 측에서 처리
    // 수신 메세지 반송 @SendTo 또한 redis에서 같이 발행되므로 생략
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageRequest request) {
        messageService.processChatMessage(request);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload MessageRequest request, SimpMessageHeaderAccessor headerAccessor) {

    }


}
