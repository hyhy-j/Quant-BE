package com.example.quantserver.auth.service;

import com.example.quantserver.auth.dto.LoginRequest;
import com.example.quantserver.auth.dto.RefreshRequest;
import com.example.quantserver.auth.dto.SignupRequest;
import com.example.quantserver.auth.dto.TokenResponse;
import com.example.quantserver.global.exception.BusinessException;
import com.example.quantserver.global.exception.ErrorCode;
import com.example.quantserver.global.jwt.JwtTokenProvider;
import com.example.quantserver.user.domain.User;
import com.example.quantserver.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String REFRESH_TOKEN_PREFIX = "RT:";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public TokenResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();

        userRepository.save(user);

        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(user.getRole().name())
        );

        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), authorities);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + user.getEmail(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        return TokenResponse.of(accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(user.getRole().name())
        );

        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), authorities);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + user.getEmail(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        return TokenResponse.of(accessToken, refreshToken);
    }

    public void logout(String email) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + email);
    }

    public TokenResponse refresh(RefreshRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String email = jwtTokenProvider.getAuthentication(refreshToken).getName();
        String savedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + email);

        if (savedToken == null || !savedToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(user.getRole().name())
        );

        String newAccessToken = jwtTokenProvider.createAccessToken(email, authorities);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(email);

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + email,
                newRefreshToken,
                jwtTokenProvider.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void withdraw(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        userRepository.delete(user);
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + email);
    }
}
