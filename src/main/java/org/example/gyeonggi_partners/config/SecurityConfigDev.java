package org.example.gyeonggi_partners.config;

import lombok.RequiredArgsConstructor;
import org.example.gyeonggi_partners.common.jwt.JwtAuthenticationFilter;
import org.example.gyeonggi_partners.common.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Profile("!prod") // 운영(prod) 프로파일이 아닐 때만 이 설정을 사용
@RequiredArgsConstructor
public class SecurityConfigDev {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // 1. 기본 인증 방식 비활성화
        http.csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화
                .formLogin(AbstractHttpConfigurer::disable) // Form 기반 로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable); // HTTP Basic 인증 비활성화

        // 2. 세션 정책을 STATELESS로 설정 (JWT는 세션을 사용하지 않음)
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 3. JWT 필터 추가
        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
        );

        // HTTP 요청에 대한 인가 규칙 설정
        http.authorizeHttpRequests(auth -> auth
                // Swagger UI 및 API 문서 관련 경로 모두 허용
                .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                ).permitAll()

                // 로그인은 인증 불필요
                .requestMatchers("/api/auth/login").permitAll()
                
                // 로그아웃은 인증 필요
                .requestMatchers("/api/auth/logout").authenticated()
                
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

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

}
