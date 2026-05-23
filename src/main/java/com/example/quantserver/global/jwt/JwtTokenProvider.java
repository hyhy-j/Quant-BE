package com.example.quantserver.global.jwt;

import com.example.quantserver.global.config.JwtProperties;
import com.example.quantserver.global.exception.BusinessException;
import com.example.quantserver.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final CustomUserDetailsService customUserDetailsService;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
    }

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    public String createAccessToken(Long userId) {
        return buildToken(String.valueOf(userId), jwtProperties.getAccessTokenExpiration(), ACCESS_TOKEN_TYPE);
    }

    public String createRefreshToken(Long userId) {
        return buildToken(String.valueOf(userId), jwtProperties.getRefreshTokenExpiration(), REFRESH_TOKEN_TYPE);
    }

    private String buildToken(String subject, long expirationMs, String tokenType) {
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        if (!ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(claims.getSubject());
        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }

    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        if (!REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public void validateTokenOrThrow(String token) {
        try {
            parseClaims(token);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    public long getRefreshTokenExpiration() {
        return jwtProperties.getRefreshTokenExpiration();
    }

    public long getRemainingExpiration(String token) {
        return parseClaims(token).getExpiration().getTime() - System.currentTimeMillis();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
