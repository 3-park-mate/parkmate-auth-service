package com.parkmate.authservice.authuser.application;

import com.parkmate.authservice.authuser.domain.AuthUser;
import com.parkmate.authservice.authuser.dto.request.UserLoginRequestDto;
import com.parkmate.authservice.authuser.dto.request.UserRegisterRequestDto;
import com.parkmate.authservice.authuser.dto.request.feign.UserRegisterRequestForUserServiceDto;
import com.parkmate.authservice.authuser.dto.response.UserLoginResponseDto;
import com.parkmate.authservice.authuser.infrastructure.AuthRepository;
import com.parkmate.authservice.authuser.infrastructure.client.UserFeignClient;
import com.parkmate.authservice.common.exception.BaseException;
import com.parkmate.authservice.common.generator.UUIDGenerator;
import com.parkmate.authservice.common.redis.RedisService;
import com.parkmate.authservice.common.response.ResponseStatus;
import com.parkmate.authservice.common.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {


    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;
    private final JwtProvider jwtProvider;
    private final UserFeignClient userFeignClient;

    @Transactional
    @Override
    public UserLoginResponseDto login(UserLoginRequestDto userLoginRequestDto) {

        AuthUser authUser = authRepository.findByEmail(userLoginRequestDto.getEmail())
                .orElseThrow(() -> new BaseException(ResponseStatus.AUTH_USER_NOT_FOUND));

        if (authUser.isAccountLocked()) {
            throw new BaseException(ResponseStatus.AUTH_ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(userLoginRequestDto.getPassword(), authUser.getPassword())) {
            authUser.increaseLoginFailCount();

            if (authUser.isAccountLocked()) {
                // 계정 잠금 알림 로직
                // e.g., 메일 발송
            }

            authRepository.save(authUser);
            throw new BaseException(ResponseStatus.INVALID_AUTH_PASSWORD);
        }

        authUser.resetLoginFailCount();
        authRepository.save(authUser);

        String accessToken = jwtProvider.generateAccessToken(authUser.getUserUuid());
        String refreshToken = jwtProvider.generateRefreshToken(authUser.getUserUuid());

        return UserLoginResponseDto.of(authUser.getUserUuid(), accessToken, refreshToken);
    }

    @Transactional
    @Override
    public void logout(String userUuid) {

        AuthUser authUser = authRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new BaseException(ResponseStatus.AUTH_USER_NOT_FOUND));
    }

    @Transactional
    @Override
    public void register(UserRegisterRequestDto userRegisterRequestDto) {

        validateEmailDuplication(userRegisterRequestDto.getEmail());
        verifyEmailCode(userRegisterRequestDto.getEmail(), userRegisterRequestDto.getVerificationCode());

        String encodedPassword = passwordEncoder.encode(userRegisterRequestDto.getPassword());
        String userUuid = UUIDGenerator.generateUUID();

        saveToAuthDatabase(userRegisterRequestDto, userUuid, encodedPassword);
        sendUserToUserService(userRegisterRequestDto, userUuid, encodedPassword);

        redisService.deleteVerificationCode(userRegisterRequestDto.getEmail());
    }

    private void validateEmailDuplication(String email) {
        if (authRepository.existsByEmail(email)) {
            throw new BaseException(ResponseStatus.AUTH_EMAIL_ALREADY_EXISTS);
        }
    }

    private void verifyEmailCode(String email, String code) {
        if (!redisService.isValidEmailVerificationCode(email, code)) {
            throw new BaseException(ResponseStatus.AUTH_VERIFICATION_FAILED);
        }
    }

    private void saveToAuthDatabase(UserRegisterRequestDto userRegisterRequestDto, String userUuid, String encodedPassword) {
        AuthUser authUser = userRegisterRequestDto.toEntity(userUuid, encodedPassword);
        authRepository.save(authUser);
    }

    private void sendUserToUserService(UserRegisterRequestDto userRegisterRequestDto, String userUuid, String encodedPassword) {
        UserRegisterRequestForUserServiceDto request =
                UserRegisterRequestForUserServiceDto.of(
                        userUuid,
                        userRegisterRequestDto.getEmail(),
                        userRegisterRequestDto.getName(),
                        userRegisterRequestDto.getPhoneNumber(),
                        0
                );
        userFeignClient.registerUser(request);
    }
}