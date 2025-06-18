package com.parkmate.authservice.common.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    // ==================== Redis Key Prefix ====================
    private static final String EMAIL_VERIFICATION_PREFIX_USER = "email:verification:code:user:";
    private static final String EMAIL_VERIFICATION_PREFIX_HOST = "email:verification:code:host:";
    private static final String LOGIN_FAIL_PREFIX_USER = "login:fail:";
    private static final String LOGIN_FAIL_PREFIX_HOST = "login:fail:host:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    private static final Duration LOGIN_FAIL_TTL = Duration.ofMinutes(15);
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(14);

    // ==================== 이메일 인증 ====================

    /**
     * 이메일 인증 코드 저장
     * @param email 대상 이메일
     * @param code 인증 코드
     * @param ttl 유효 시간
     * @param isHost 호스트 여부
     */
    public void saveVerificationCode(String email, String code, Duration ttl, boolean isHost) {
        String key = buildEmailVerificationKey(email, isHost);
        redisTemplate.opsForValue().set(key, code, ttl.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * 이메일 인증 코드 검증
     */
    public boolean verifyEmailCode(String email, String code, boolean isHost) {

        if (email == null || code == null) {
            throw new IllegalArgumentException("이메일 또는 인증코드가 null입니다.");
        }

        String key = buildEmailVerificationKey(email, isHost);
        String storedCode = redisTemplate.opsForValue().get(key);

        return storedCode != null && storedCode.equals(code);
    }

    /**
     * 이메일 인증 코드 조회
     */
    public String getVerificationCode(String email, boolean isHost) {
        return redisTemplate.opsForValue().get(buildEmailVerificationKey(email, isHost));
    }

    /**
     * 이메일 인증 코드 삭제
     */
    public void deleteVerificationCode(String email, boolean isHost) {
        redisTemplate.delete(buildEmailVerificationKey(email, isHost));
    }

    // ==================== 로그인 실패 카운트 ====================

    public int incrementUserLoginFailCount(String email) {
        String key = buildUserLoginFailKey(email);
        Long count = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, LOGIN_FAIL_TTL);
        return count != null ? count.intValue() : 0;
    }

    public void resetUserLoginFailCount(String email) {
        redisTemplate.delete(buildUserLoginFailKey(email));
    }

    public int incrementHostLoginFailCount(String email) {
        String key = buildHostLoginFailKey(email);
        Long count = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, LOGIN_FAIL_TTL);
        return count != null ? count.intValue() : 0;
    }

    public void resetHostLoginFailCount(String email) {
        redisTemplate.delete(buildHostLoginFailKey(email));
    }

    // ==================== 리프레시 토큰 ====================

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

    // ==================== Key 생성 메서드 ====================

    private String buildEmailVerificationKey(String email, boolean isHost) {
        return (isHost ? EMAIL_VERIFICATION_PREFIX_HOST : EMAIL_VERIFICATION_PREFIX_USER) + email.trim();
    }

    private String buildUserLoginFailKey(String email) {
        return LOGIN_FAIL_PREFIX_USER + email.trim();
    }

    private String buildHostLoginFailKey(String email) {
        return LOGIN_FAIL_PREFIX_HOST + email.trim();
    }

    private String buildRefreshTokenKey(String userUuid) {
        return REFRESH_TOKEN_PREFIX + userUuid;
    }
}