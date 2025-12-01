package org.example.gyeonggipartners.domain.proposal.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 증거자료(Evidence) Value Object
 *
 * <p><strong>[설계 의도: 정적 팩토리 메서드 & 불변 객체]</strong></p>
 * <ul>
 * <li><strong>생성 제어:</strong> {@code private} 생성자로 외부에서의 무분별한 객체 생성을 차단합니다.</li>
 * <li><strong>무결성 보장:</strong> 오직 {@code of()} 메서드를 통해서만 생성 가능하며, 생성 시점에 유효성 검증을 강제합니다.</li>
 * <li><strong>불변성:</strong> 생성 후에는 상태를 변경할 수 없습니다.</li>
 * </ul>
 *
 * <p>참고: 데이터베이스에는 JSONB 타입으로 저장됩니다.</p>
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Evidence {

    private EvidenceType type;
    private String title;
    private String url;

    /** * [정적 팩토리 메서드]
     * 외부에서 값을 받아 검증 후, 안전한 객체만을 생성하여 반환합니다.
     */
    public static Evidence of(EvidenceType type, String title, String url) {
        validateTitle(title);
        validateUrl(url);
        return new Evidence(type, title, url);
    }

    private static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("증거자료 제목은 필수입니다.");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("증거자료 제목은 100자를 초과할 수 없습니다.");
        }
    }

    private static void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("증거자료 URL은 필수입니다.");
        }
        if (url.length() > 500) {
            throw new IllegalArgumentException("증거자료 URL은 500자를 초과할 수 없습니다.");
        }
    }
}
