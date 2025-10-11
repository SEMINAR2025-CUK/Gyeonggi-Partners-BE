package org.example.gyeonggi_partners.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정 클래스
 */
@Configuration
public class RedisConfig {

    /**
     * RedisTemplate 빈 설정
     * 
     * <p>이 설정이 없으면 기본 Java 직렬화를 사용하여 데이터가 이진 형태로 저장됩니다.
     * JSON 직렬화를 설정하여 Redis CLI에서 데이터 확인 및 디버깅이 용이하도록 합니다.</p>
     * 
     * <ul>
     *   <li>Key: String 형식 (예: "user:1")</li>
     *   <li>Value: JSON 형식 (예: {"id":1,"name":"홍길동"})</li>
     * </ul>
     *
     * @param connectionFactory Redis 연결 팩토리
     * @return JSON 직렬화가 설정된 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key는 String으로 직렬화
        template.setKeySerializer(new StringRedisSerializer());
        // Value는 JSON으로 직렬화
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // Hash Key도 String으로 직렬화
        template.setHashKeySerializer(new StringRedisSerializer());
        // Hash Value도 JSON으로 직렬화
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
