package org.example.gyeonggi_partners.domain.message.api.dto;

import lombok.Getter;
import org.example.gyeonggi_partners.domain.message.api.MessageType;

@Getter
public class MessageRequest {
    private MessageType type;
    private String content;
    private Long roomId;
    private Long userId;
}
