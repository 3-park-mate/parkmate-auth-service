package com.parkmate.authservice.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String EMAIL_VERIFICATION_PREFIX = "email:verification:code:";
    private static final String LOGIN_FAIL_PREFIX = "login:fail:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    private static final Duration LOGIN_FAIL_TTL = Duration.ofMinutes(15);
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(14);

    public void saveVerificationCode(String email, String code, Duration ttl) {
        redisTemplate.opsForValue().set(buildEmailVerificationKey(email), code, ttl.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void deleteVerificationCode(String email) {
        redisTemplate.delete(buildEmailVerificationKey(email));
    }

    public int incrementLoginFailCount(String email) {
        String key = buildLoginFailKey(email);
        Long count = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, LOGIN_FAIL_TTL);
        return count != null ? count.intValue() : 0;
    }

    public void resetLoginFailCount(String email) {
        redisTemplate.delete(buildLoginFailKey(email));
    }

    public void saveRefreshToken(String userUuid, String refreshToken, long ttlMillis) {
        redisTemplate.opsForValue().set(
                buildRefreshTokenKey(userUuid),
                refreshToken,
                ttlMillis,
                TimeUnit.MILLISECONDS
        );
    }

    public void deleteRefreshToken(String userUuid) {
        redisTemplate.delete(buildRefreshTokenKey(userUuid));
    }

    public String getVerificationCode(String email) {
        return redisTemplate.opsForValue().get(buildEmailVerificationKey(email));
    }

    // ==================== Key 생성 메서드 ====================

    private String buildEmailVerificationKey(String email) {
        return EMAIL_VERIFICATION_PREFIX + email.trim();
    }

    private String buildLoginFailKey(String email) {
        return LOGIN_FAIL_PREFIX + email.trim();
    }

    private String buildRefreshTokenKey(String userUuid) {
        return REFRESH_TOKEN_PREFIX + userUuid;
    }
}