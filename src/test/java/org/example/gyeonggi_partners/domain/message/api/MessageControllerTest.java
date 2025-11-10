package org.example.gyeonggi_partners.domain.message.api;

import org.example.gyeonggi_partners.domain.message.api.dto.MessagePageResponse;
import org.example.gyeonggi_partners.domain.message.api.dto.MessageResponse;
import org.example.gyeonggi_partners.domain.message.application.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@DisplayName("MessageController 단위 테스트")
class MessageControllerTest {

    private final MessageService messageService = Mockito.mock(MessageService.class);
    private final MessageController messageController = new MessageController(messageService);

    @Test
    @DisplayName("메시지 조회 - 커서 없이 조회")
    void getMessages_WithoutCursor() {
        // given
        Long roomId = 1L;
        int size = 30;
        MessagePageResponse mockResponse = createMockPageResponse(20, false);

        given(messageService.getMessages(eq(roomId), isNull(), eq(size)))
                .willReturn(mockResponse);

        // when
        var response = messageController.getMessages(roomId, null, size);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("SUCCESS");
        assertThat(response.getBody().data().getMessages()).hasSize(20);
        assertThat(response.getBody().data().isHasNext()).isFalse();
    }

    @Test
    @DisplayName("메시지 조회 - 커서 기반 페이징")
    void getMessages_WithCursor() {
        // given
        Long roomId = 1L;
        Long cursor = 100L;
        int size = 30;
        MessagePageResponse mockResponse = createMockPageResponse(30, true);

        given(messageService.getMessages(eq(roomId), eq(cursor), eq(size)))
                .willReturn(mockResponse);

        // when
        var response = messageController.getMessages(roomId, cursor, size);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("SUCCESS");
        assertThat(response.getBody().data().getMessages()).hasSize(30);
        assertThat(response.getBody().data().isHasNext()).isTrue();
        assertThat(response.getBody().data().getNextCursor()).isNotNull();
    }

    @Test
    @DisplayName("메시지 조회 - 기본 size 값 적용")
    void getMessages_DefaultSize() {
        // given
        Long roomId = 1L;
        MessagePageResponse mockResponse = createMockPageResponse(30, false);

        given(messageService.getMessages(eq(roomId), isNull(), eq(30)))
                .willReturn(mockResponse);

        // when
        var response = messageController.getMessages(roomId, null, 30);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("메시지 조회 - 빈 결과")
    void getMessages_EmptyResult() {
        // given
        Long roomId = 1L;
        MessagePageResponse emptyResponse = createMockPageResponse(0, false);

        given(messageService.getMessages(eq(roomId), isNull(), anyInt()))
                .willReturn(emptyResponse);

        // when
        var response = messageController.getMessages(roomId, null, 30);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("SUCCESS");
        assertThat(response.getBody().data().getMessages()).isEmpty();
        assertThat(response.getBody().data().isHasNext()).isFalse();
    }

    // Helper Methods
    private MessagePageResponse createMockPageResponse(int messageCount, boolean hasNext) {
        List<MessageResponse> messages = new ArrayList<>();
        for (int i = 1; i <= messageCount; i++) {
            messages.add(MessageResponse.builder()
                    .id((long) i)
                    .content("메시지 " + i)
                    .userName("테스트유저")
                    .createdAt(LocalDateTime.now())
                    .build());
        }
        
        Long nextCursor = hasNext && !messages.isEmpty() ? messages.get(messages.size() - 1).getId() : null;
        
        return MessagePageResponse.builder()
                .messages(messages)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }
}
