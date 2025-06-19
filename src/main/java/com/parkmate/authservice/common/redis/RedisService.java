package com.parkmate.authservice.common.redis;

import com.parkmate.authservice.common.roletype.RoleType;
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
    private static final String VERIFY_FAIL_PREFIX_USER = "verify:fail:user:";
    private static final String VERIFY_FAIL_PREFIX_HOST = "verify:fail:host:";
    private static final String LOGIN_FAIL_PREFIX_USER = "login:fail:user:";
    private static final String LOGIN_FAIL_PREFIX_HOST = "login:fail:host:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    // ==================== TTL 설정 ====================
    private static final Duration LOGIN_FAIL_TTL = Duration.ofMinutes(15);
    private static final Duration VERIFY_FAIL_BLOCK_TTL = Duration.ofMinutes(10);
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(14);
    private static final int VERIFY_FAIL_LIMIT = 5;

    // ==================== 이메일 인증 코드 ====================

    public void saveVerificationCode(String email, String code, Duration ttl, RoleType roleType) {
        redisTemplate.opsForValue().set(buildEmailVerificationKey(email, roleType), code, ttl.toMillis(), TimeUnit.MILLISECONDS);
    }

    public boolean verifyEmailCode(String email, String code, RoleType roleType) {
        if (email == null || code == null) {
            throw new IllegalArgumentException("이메일 또는 인증코드가 null입니다.");
        }
        String storedCode = redisTemplate.opsForValue().get(buildEmailVerificationKey(email, roleType));
        return storedCode != null && storedCode.equals(code);
    }

    public String getVerificationCode(String email, RoleType roleType) {
        return redisTemplate.opsForValue().get(buildEmailVerificationKey(email, roleType));
    }

    public void deleteVerificationCode(String email, RoleType roleType) {
        redisTemplate.delete(buildEmailVerificationKey(email, roleType));
    }

    // ==================== 인증 시도 실패 및 차단 관리 ====================

    public int incrementVerificationAttemptFailCount(String email, RoleType roleType) {
        String key = buildVerificationAttemptFailKey(email, roleType);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, VERIFY_FAIL_BLOCK_TTL);
        }
        return count != null ? count.intValue() : 0;
    }

    public void resetVerificationAttemptFailCount(String email, RoleType roleType) {
        redisTemplate.delete(buildVerificationAttemptFailKey(email, roleType));
    }

    public boolean isVerificationAttemptBlocked(String email, RoleType roleType) {
        String countStr = redisTemplate.opsForValue().get(buildVerificationAttemptFailKey(email, roleType));
        if (countStr == null) return false;
        try {
            return Integer.parseInt(countStr) >= VERIFY_FAIL_LIMIT;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void blockVerificationAttempts(String email, RoleType roleType, Duration duration) {
        redisTemplate.expire(buildVerificationAttemptFailKey(email, roleType), duration);
    }

    // ==================== 로그인 실패 카운트 ====================

    public int incrementLoginFailCount(String email, RoleType roleType) {
        String key = buildLoginFailKey(email, roleType);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, LOGIN_FAIL_TTL);
        }
        return count != null ? count.intValue() : 0;
    }

    public void resetLoginFailCount(String email, RoleType roleType) {
        redisTemplate.delete(buildLoginFailKey(email, roleType));
    }

    // ==================== 리프레시 토큰 ====================

    public void saveRefreshToken(String userUuid, String refreshToken, long ttlMillis) {
        redisTemplate.opsForValue().set(buildRefreshTokenKey(userUuid), refreshToken, ttlMillis, TimeUnit.MILLISECONDS);
    }

    public void deleteRefreshToken(String userUuid) {
        redisTemplate.delete(buildRefreshTokenKey(userUuid));
    }

    // ==================== Key 생성 메서드 ====================

    private String buildEmailVerificationKey(String email, RoleType roleType) {
        return (roleType == RoleType.HOST ? EMAIL_VERIFICATION_PREFIX_HOST : EMAIL_VERIFICATION_PREFIX_USER) + email.trim();
    }

    private String buildVerificationAttemptFailKey(String email, RoleType roleType) {
        return (roleType == RoleType.HOST ? VERIFY_FAIL_PREFIX_HOST : VERIFY_FAIL_PREFIX_USER) + email.trim();
    }

    private String buildLoginFailKey(String email, RoleType roleType) {
        return (roleType == RoleType.HOST ? LOGIN_FAIL_PREFIX_HOST : LOGIN_FAIL_PREFIX_USER) + email.trim();
    }

    private String buildRefreshTokenKey(String userUuid) {
        return REFRESH_TOKEN_PREFIX + userUuid;
    }
}
