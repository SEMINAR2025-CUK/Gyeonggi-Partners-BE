package org.example.gyeonggi_partners.domain.message.api;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * WebSocket 실제 통합 테스트
 * 
 * ⚠️ 주의: @SpringBootTest를 사용한 전체 애플리케이션 테스트는
 * 모든 Bean(JWT, Email, DB, Redis 등)이 필요하여 테스트 환경 설정이 복잡합니다.
 * 
 * 🎯 실제 WebSocket 동작 확인 방법:
 * 
 * 1. 환경 구동
 *    docker-compose up -d
 * 
 * 2. 애플리케이션 실행
 *    .\gradlew.bat bootRun
 * 
 * 3. WebSocket 클라이언트로 테스트
 *    (예: Postman WebSocket, 브라우저 JavaScript)
 * 
 * JavaScript 예제:
 * ```javascript
 * const socket = new SockJS('http://localhost:8080/ws');
 * const stompClient = Stomp.over(socket);
 * 
 * stompClient.connect({}, function() {
 *     // 구독
 *     stompClient.subscribe('/topic/chat/room/1', function(message) {
 *         console.log('받은 메시지:', message.body);
 *     });
 *     
 *     // 전송
 *     stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
 *         type: 'CHAT',
 *         content: '테스트 메시지',
 *         roomId: 1,
 *         userId: 1
 *     }));
 * });
 * ```
 */
@Disabled("통합 테스트는 전체 환경 설정이 필요하므로 비활성화. 실제 확인은 수동 테스트로 진행")
@DisplayName("메시지 WebSocket 통합 테스트 (수동 테스트 권장)")
class MessageWebSocketIntegrationTest {

    @Test
    @DisplayName("이 테스트는 비활성화되어 있습니다")
    void testWebSocketConnection() {
        System.out.println("=".repeat(70));
        System.out.println("📌 WebSocket 실제 동작 확인 방법:");
        System.out.println("=".repeat(70));
        System.out.println();
        System.out.println("1️⃣ 환경 구동:");
        System.out.println("   docker-compose up -d");
        System.out.println();
        System.out.println("2️⃣ 애플리케이션 실행:");
        System.out.println("   .\\gradlew.bat bootRun");
        System.out.println();
        System.out.println("3️⃣ 브라우저 콘솔에서 테스트:");
        System.out.println("   const socket = new SockJS('http://localhost:8080/ws');");
        System.out.println("   const stomp = Stomp.over(socket);");
        System.out.println("   stomp.connect({}, () => {");
        System.out.println("       stomp.subscribe('/topic/chat/room/1', msg => console.log(msg));");
        System.out.println("       stomp.send('/app/chat.sendMessage', {}, JSON.stringify({");
        System.out.println("           type: 'CHAT', content: '테스트', roomId: 1, userId: 1");
        System.out.println("       }));");
        System.out.println("   });");
        System.out.println();
        System.out.println("4️⃣ 또는 Postman WebSocket 기능 사용");
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println("✅ 단위 테스트로 로직을 검증하고,");
        System.out.println("✅ 실제 WebSocket은 위 방법으로 확인하세요!");
        System.out.println("=".repeat(70));
    }
}
