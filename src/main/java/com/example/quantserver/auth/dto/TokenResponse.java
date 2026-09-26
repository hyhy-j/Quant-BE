package com.example.quantserver.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        String nickname
) {
    public static TokenResponse of(String accessToken, String refreshToken) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", null);
    }

    public static TokenResponse of(String accessToken, String refreshToken, String nickname) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", nickname);
    }
}
