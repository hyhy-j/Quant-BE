package com.example.quantserver.auth.service;

import com.example.quantserver.auth.dto.LoginRequest;
import com.example.quantserver.auth.dto.RefreshRequest;
import com.example.quantserver.auth.dto.SignupRequest;
import com.example.quantserver.auth.dto.TokenResponse;
import com.example.quantserver.global.common.RedisKeys;
import com.example.quantserver.global.exception.BusinessException;
import com.example.quantserver.global.exception.ErrorCode;
import com.example.quantserver.global.jwt.JwtTokenProvider;
import com.example.quantserver.user.entity.User;
import com.example.quantserver.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String CAS_REFRESH_TOKEN_SCRIPT = """
            local stored = redis.call('GET', KEYS[1])
            if stored == ARGV[1] then
                redis.call('SET', KEYS[1], ARGV[2])
                redis.call('PEXPIRE', KEYS[1], ARGV[3])
                return 1
            else
                return 0
            end
            """;

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

        return issueTokens(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return issueTokens(user);
    }

    public void logout(Long userId, String accessToken) {
        redisTemplate.delete(RedisKeys.REFRESH_TOKEN_PREFIX + userId);
        long remaining = jwtTokenProvider.getRemainingExpiration(accessToken);
        if (remaining > 0) {
            redisTemplate.opsForValue().set(
                    RedisKeys.BLACKLIST_PREFIX + accessToken,
                    "logout",
                    remaining,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    public TokenResponse refresh(RefreshRequest request) {
        String refreshToken = request.refreshToken();

        jwtTokenProvider.validateTokenOrThrow(refreshToken);

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

        Long result = redisTemplate.execute(
                new DefaultRedisScript<>(CAS_REFRESH_TOKEN_SCRIPT, Long.class),
                List.of(RedisKeys.REFRESH_TOKEN_PREFIX + userId),
                refreshToken,
                newRefreshToken,
                String.valueOf(jwtTokenProvider.getRefreshTokenExpiration())
        );

        if (!Long.valueOf(1L).equals(result)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        userRepository.delete(user);
        redisTemplate.delete(RedisKeys.REFRESH_TOKEN_PREFIX + userId);
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        redisTemplate.opsForValue().set(
                RedisKeys.REFRESH_TOKEN_PREFIX + user.getId(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        return TokenResponse.of(accessToken, refreshToken);
    }
}
