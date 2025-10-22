package org.example.gyeonggi_partners.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("!prod") // 운영(prod) 프로파일이 아닐 때만 이 설정을 사용
public class SecurityConfigDev {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF 보호 비활성화 (개발 초기)
        http.csrf(AbstractHttpConfigurer::disable);

        // HTTP 요청에 대한 인가 규칙 설정
        http.authorizeHttpRequests(auth -> auth
                // Swagger UI 및 API 문서 관련 경로 모두 허용
                .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                ).permitAll()

                // Actuator 헬스 체크 경로 허용
                .requestMatchers("/actuator/health/**").permitAll()

                // 개발 환경에서는 나머지 모든 요청을 허용하여 빠른 테스트를 지원
                .anyRequest().permitAll()
        );

        return http.build();
    }

    /**
     * 비밀번호 암호화를 위한 PasswordEncoder Bean
     * BCrypt 해싱 알고리즘 사용
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
