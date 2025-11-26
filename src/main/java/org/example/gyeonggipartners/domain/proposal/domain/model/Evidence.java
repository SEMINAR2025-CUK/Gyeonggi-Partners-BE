package org.example.gyeonggipartners.domain.proposal.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 증거자료 Value Object
 * JSONB로 저장됨
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Evidence {

    private EvidenceType type;
    private String title;
    private String url;

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
