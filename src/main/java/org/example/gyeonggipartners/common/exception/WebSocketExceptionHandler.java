package org.example.gyeonggipartners.common.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.gyeonggipartners.common.dto.ApiResponse;
import org.example.gyeonggipartners.domain.message.exception.MessageErrorCode;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
// restControllerAdivce는 REST API 전용임
// webSocket의 예외를 잡기 위해 ControllerAdvice 사용
public class WebSocketExceptionHandler {

    // 사용자가 정의한 예외 chatException 발생시 이 핸들러로 캐치
    @MessageExceptionHandler(MessageException.class)
    @SendToUser("/queue/errors") // 유저 개인 에러 큐로 dto 전송
    public ResponseEntity<ApiResponse<ErrorCode>> handleMessageException(MessageException e) {
        final ErrorCode errorCode = e.getErrorCode();
        log.error("handleMessageException: {}", errorCode.getMessage());

        ApiResponse<ErrorCode> response=ApiResponse.error(errorCode);
        return new ResponseEntity<>(response,HttpStatusCode.valueOf(errorCode.getStatus()));
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public ResponseEntity<ApiResponse<ErrorCode>> handleUnknownException(Exception e) {

        log.error("handleUnhandledException: {}", e.getMessage());
        ErrorCode errorCode= MessageErrorCode.INTERNAL_SERVER_ERROR;
        ApiResponse<ErrorCode> response=ApiResponse.error(errorCode);

        return new ResponseEntity<>(response,HttpStatusCode.valueOf(errorCode.getStatus()));
    }



}
