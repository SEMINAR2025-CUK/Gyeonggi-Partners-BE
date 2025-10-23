package org.example.gyeonggi_partners.domain.user.api;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.gyeonggi_partners.common.dto.ApiResponse;
import org.example.gyeonggi_partners.domain.user.api.dto.SignInRequest;
import org.example.gyeonggi_partners.domain.user.api.dto.SignInResponse;
import org.example.gyeonggi_partners.domain.user.application.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "사용자 인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "로그인",
            description = "사용자 로그인을 수행하고 JWT 토큰을 발급합니다."
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<SignInResponse>> login(
            @Valid @RequestBody SignInRequest request) {

        SignInResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(response,"로그인에 성공했습니다.")
        );
    }
}