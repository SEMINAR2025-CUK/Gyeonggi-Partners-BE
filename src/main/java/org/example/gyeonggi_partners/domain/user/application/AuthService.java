package org.example.gyeonggi_partners.domain.user.application;


import lombok.RequiredArgsConstructor;
import org.example.gyeonggi_partners.common.dto.TokenDto;
import org.example.gyeonggi_partners.common.exception.BusinessException;
import org.example.gyeonggi_partners.common.jwt.CustomUserDetails;
import org.example.gyeonggi_partners.common.jwt.JwtTokenProvider;
import org.example.gyeonggi_partners.domain.user.api.dto.SignInRequest;
import org.example.gyeonggi_partners.domain.user.api.dto.SignInResponse;
import org.example.gyeonggi_partners.domain.user.exception.UserErrorCode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "RT:";

    public SignInResponse login(SignInRequest request) {

        try {
            // 1. 인증 토큰 생성
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            request.getLoginId(),
                            request.getPassword()
                    );

            // 2. Spring Security 인증
            Authentication authentication =
                    authenticationManager.authenticate(authToken);

            // 3. JWT 토큰 생성
            TokenDto tokenDto = jwtTokenProvider.generateTokenDto(authentication);

            // 4. CustomUserDetails 추출
            CustomUserDetails userDetails =
                    (CustomUserDetails) authentication.getPrincipal();

            // 5. Refresh Token을 Redis에 저장
            redisTemplate.opsForValue().set(
                    REFRESH_TOKEN_PREFIX + userDetails.getUserId(),
                    tokenDto.getRefreshToken(),
                    7,
                    TimeUnit.DAYS
            );

            // 6. 응답 생성
            return new SignInResponse(
                    userDetails.getUserId(),
                    userDetails.getNickname(),
                    userDetails.getEmail(),
                    userDetails.getRole(),
                    tokenDto.getGrantType(),
                    tokenDto.getAccessToken(),
                    tokenDto.getRefreshToken()
            );

        } catch (BadCredentialsException | UsernameNotFoundException |
                 InternalAuthenticationServiceException e) {
            // ⭐ Spring Security 예외를 BusinessException으로 변환
            throw new BusinessException(UserErrorCode.LOGIN_FAILED);
        }
    }

}