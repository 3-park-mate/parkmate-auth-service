package com.parkmate.authservice.authuser.application;

import com.parkmate.authservice.authuser.application.policy.AuthUserPolicyService;
import com.parkmate.authservice.authuser.domain.AuthUser;
import com.parkmate.authservice.authuser.dto.request.UserLoginRequestDto;
import com.parkmate.authservice.authuser.dto.request.UserRegisterRequestDto;
import com.parkmate.authservice.authuser.dto.request.feign.UserRegisterRequestForUserServiceDto;
import com.parkmate.authservice.authuser.dto.response.UserLoginResponseDto;
import com.parkmate.authservice.authuser.infrastructure.AuthRepository;
import com.parkmate.authservice.authuser.infrastructure.client.UserFeignClient;
import com.parkmate.authservice.common.exception.BaseException;
import com.parkmate.authservice.common.generator.UUIDGenerator;
import com.parkmate.authservice.common.mail.MailService;
import com.parkmate.authservice.common.redis.RedisService;
import com.parkmate.authservice.common.response.ResponseStatus;
import com.parkmate.authservice.common.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {


    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;
    private final JwtProvider jwtProvider;
    private final UserFeignClient userFeignClient;
    private final MailService mailService;
    private final AuthenticationManager authenticationManager;
    private final AuthUserPolicyService authUserPolicyService;

    @Transactional
    @Override
    public UserLoginResponseDto login(UserLoginRequestDto userLoginRequestDto) {

        AuthUser user = authRepository.findByEmail(userLoginRequestDto.getEmail())
                .orElseThrow(() -> new BaseException(ResponseStatus.AUTH_USER_NOT_FOUND));

        if (authUserPolicyService.isAccountLocked(user)) {

            throw new BaseException(ResponseStatus.AUTH_ACCOUNT_LOCKED);
        }

        if (!userLoginRequestDto.isPasswordMatch(user.getPassword(), passwordEncoder)) {

            int failCount = redisService.incrementLoginFailCount(userLoginRequestDto.getEmail());

            if (authUserPolicyService.shouldLockAccount(failCount)) {

                user.lockAccount();
                authRepository.save(user);

                String userName = userFeignClient.findNameByEmail(user.getEmail());
                mailService.sendAccountLockEmail(user.getEmail(), userName);
            }

            throw new BaseException(ResponseStatus.INVALID_AUTH_PASSWORD);
        }

        redisService.resetLoginFailCount(userLoginRequestDto.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userLoginRequestDto.getEmail(),
                        userLoginRequestDto.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtProvider.generateAccessToken(user.getUserUuid());
        String refreshToken = jwtProvider.generateRefreshToken(user.getUserUuid());

        redisService.saveRefreshToken(user.getUserUuid(), refreshToken, Duration.ofDays(7).toMillis());

        return UserLoginResponseDto.of(user.getUserUuid(), accessToken, refreshToken);
    }

    @Transactional
    @Override
    public void logout(String userUuid) {

        redisService.deleteRefreshToken(userUuid);
    }

    @Transactional
    @Override
    public void register(UserRegisterRequestDto userRegisterRequestDto) {

        String userUuid = UUIDGenerator.generateUUID();
        AuthUser user = userRegisterRequestDto.toEntity(userUuid, passwordEncoder);

        try {
            authRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new BaseException(ResponseStatus.AUTH_EMAIL_ALREADY_EXISTS);
        }

        try {
            UserRegisterRequestForUserServiceDto req = UserRegisterRequestForUserServiceDto.of(

                    userUuid,
                    userRegisterRequestDto.getEmail(),
                    userRegisterRequestDto.getName(),
                    userRegisterRequestDto.getPhoneNumber()
            );
            userFeignClient.registerUser(req);

        } catch (Exception e) {

            authRepository.deleteById(user.getId());
            throw new BaseException(ResponseStatus.AUTH_USER_SERVICE_ERROR);
        }

        redisService.deleteVerificationCode(userRegisterRequestDto.getEmail());
    }
}