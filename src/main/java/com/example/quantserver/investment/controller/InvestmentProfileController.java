package com.example.quantserver.investment.controller;

import com.example.quantserver.global.jwt.CustomUserDetails;
import com.example.quantserver.global.response.ApiResponse;
import com.example.quantserver.investment.dto.InvestmentProfileRequest;
import com.example.quantserver.investment.dto.InvestmentProfileResponse;
import com.example.quantserver.investment.service.InvestmentProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Investment", description = "투자 성향 프로필 API")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class InvestmentProfileController {

    private final InvestmentProfileService profileService;

    @Operation(summary = "현재 투자 성향 프로필 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "프로필 없음")
    })
    @GetMapping
    public ApiResponse<InvestmentProfileResponse> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(profileService.getCurrentProfile(userDetails.getId()));
    }

    @Operation(summary = "투자 성향 설문 제출", description = "최초 1회 설문을 완료하고 프로필을 저장합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "제출 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 프로필 존재")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InvestmentProfileResponse> submitProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody InvestmentProfileRequest request) {
        return ApiResponse.success(profileService.submitProfile(userDetails.getId(), request));
    }

    @Operation(summary = "투자 성향 프로필 수정", description = "설문을 재수행하여 프로필을 업데이트합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "프로필 없음")
    })
    @PutMapping
    public ApiResponse<InvestmentProfileResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody InvestmentProfileRequest request) {
        return ApiResponse.success(profileService.updateProfile(userDetails.getId(), request));
    }
}