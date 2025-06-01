package com.parkmate.authservice.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String EMAIL_VERIFICATION_PREFIX = "email:verification:code:";
    private static final String LOGOUT_ACCESS_TOKEN_PREFIX = "logout:";

    /**
     * 이메일 인증 코드 저장
     * @param email 이메일 주소
     * @param code 인증 코드
     * @param millis 만료 시간(ms)
     */
    public void saveVerificationCode(String email, String code, long millis) {
        redisTemplate.opsForValue().set(buildEmailVerificationKey(email), code, millis, TimeUnit.MILLISECONDS);
    }

    /**
     * 이메일 인증 코드 검증
     * @param email 이메일 주소
     * @param inputCode 사용자 입력 인증 코드
     * @return 검증 결과 (true: 일치, false: 불일치 또는 없음)
     */
    public boolean isValidEmailVerificationCode(String email, String inputCode) {
        String key = buildEmailVerificationKey(email);
        String storedCode = redisTemplate.opsForValue().get(key);
        return inputCode != null && inputCode.equals(storedCode);
    }

    /**
     * 이메일 인증 코드 삭제
     * @param email 이메일 주소
     */
    public void deleteVerificationCode(String email) {
        redisTemplate.delete(buildEmailVerificationKey(email));
    }

    /**
     * 이메일 인증용 Redis 키 생성
     * @param email 이메일
     * @return Redis 키 (prefix 포함)
     */
    private String buildEmailVerificationKey(String email) {
        return EMAIL_VERIFICATION_PREFIX + email.trim();
    }
}