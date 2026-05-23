package com.example.quantserver.auth.controller;

import com.example.quantserver.auth.dto.LoginRequest;
import com.example.quantserver.auth.dto.RefreshRequest;
import com.example.quantserver.auth.dto.SignupRequest;
import com.example.quantserver.auth.dto.TokenResponse;
import com.example.quantserver.auth.service.AuthService;
import com.example.quantserver.global.jwt.CustomUserDetails;
import com.example.quantserver.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "Auth", description = "회원 인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임으로 회원가입하며 즉시 로그인 토큰을 발급합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일")
    })
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TokenResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.success(authService.signup(request));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 액세스 토큰과 리프레시 토큰을 발급합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    })
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @Operation(summary = "로그아웃", description = "리프레시 토큰을 만료시킵니다. Authorization 헤더에 액세스 토큰이 필요합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) {
        String accessToken = resolveToken(request);
        authService.logout(userDetails.getId(), accessToken);
        return ApiResponse.success();
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    @Operation(summary = "토큰 재발급", description = "리프레시 토큰으로 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 리프레시 토큰")
    })
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.success(authService.refresh(request));
    }

    @Operation(summary = "회원탈퇴", description = "계정을 삭제합니다. Authorization 헤더에 액세스 토큰이 필요합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원탈퇴 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @DeleteMapping("/withdraw")
    public ApiResponse<Void> withdraw(@AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.withdraw(userDetails.getId());
        return ApiResponse.success();
    }
}
