package com.parkmate.authservice.authuser.application;

import com.parkmate.authservice.authuser.application.policy.AuthUserPolicyService;
import com.parkmate.authservice.authuser.domain.AuthUser;
import com.parkmate.authservice.authuser.domain.LoginType;
import com.parkmate.authservice.authuser.domain.SocialProvider;
import com.parkmate.authservice.authuser.dto.request.UserLoginRequestDto;
import com.parkmate.authservice.authuser.dto.request.UserRegisterRequestDto;
import com.parkmate.authservice.authuser.dto.request.feign.UserRegisterRequestForUserServiceDto;
import com.parkmate.authservice.authuser.dto.response.SocialLoginResponseDto;
import com.parkmate.authservice.authuser.dto.response.UserLoginResponseDto;
import com.parkmate.authservice.authuser.infrastructure.AuthRepository;
import com.parkmate.authservice.authuser.infrastructure.client.SocialOAuthClient;
import com.parkmate.authservice.authuser.infrastructure.client.UserFeignClient;
import com.parkmate.authservice.authuser.vo.request.SocialRegisterRequestVo;
import com.parkmate.authservice.authuser.vo.request.UserRegisterRequestVo;
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
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;

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
    private final SocialOAuthClient socialOAuthClient;

    private static final long REFRESH_TOKEN_EXPIRY_MILLIS = Duration.ofDays(7).toMillis();
    private static final Duration EMAIL_VERIFICATION_TTL = Duration.ofMinutes(3);


    @Transactional
    @Override
    public UserLoginResponseDto login(UserLoginRequestDto userLoginRequestDto) {

        AuthUser user = authRepository.findByEmail(userLoginRequestDto.getEmail())
                .orElseThrow(() -> new BaseException(ResponseStatus.AUTH_USER_NOT_FOUND));

        validateAccountNotLocked(user);

        if (!userLoginRequestDto.isPasswordMatch(user.getPassword(), passwordEncoder)) {
            handleFailedLogin(user);
            throw new BaseException(ResponseStatus.INVALID_AUTH_PASSWORD);
        }

        resetLoginFailure(user.getEmail());

        authenticateUser(userLoginRequestDto);

        String accessToken = jwtProvider.generateAccessToken(user.getUserUuid());
        String refreshToken = jwtProvider.generateRefreshToken(user.getUserUuid());

        redisService.saveRefreshToken(user.getUserUuid(), refreshToken, REFRESH_TOKEN_EXPIRY_MILLIS);

        return UserLoginResponseDto.of(user.getUserUuid(), accessToken, refreshToken);
    }

    @Transactional
    @Override
    public void logout(String userUuid) {
        redisService.deleteRefreshToken(userUuid);
    }

    @Transactional
    @Override
    public void register(UserRegisterRequestDto userRegisterRequestDto, UserRegisterRequestVo userRegisterRequestVo) {

        String userUuid = UUIDGenerator.generateUUID();
        AuthUser newUser = userRegisterRequestDto.toEntity(userUuid, passwordEncoder);

        saveUserOrThrow(newUser);
        try {
            UserRegisterRequestForUserServiceDto dto = UserRegisterRequestForUserServiceDto.of(
                    userUuid,
                    userRegisterRequestVo.getName(),
                    userRegisterRequestVo.getPhoneNumber()
            );
            userFeignClient.registerUser(dto);
        } catch (Exception e) {
            authRepository.deleteById(newUser.getId());
            throw new BaseException(ResponseStatus.AUTH_USER_SERVICE_ERROR);
        }

        redisService.deleteVerificationCode(userRegisterRequestDto.getEmail());
    }

    // 🔹 분리된 유틸성 메서드들

    private void validateAccountNotLocked(AuthUser authUser) {
        if (authUserPolicyService.isAccountLocked(authUser)) {
            throw new BaseException(ResponseStatus.AUTH_ACCOUNT_LOCKED);
        }
    }

    private void handleFailedLogin(AuthUser authUser) {
        int failCount = redisService.incrementLoginFailCount(authUser.getEmail());

        if (authUserPolicyService.shouldLockAccount(failCount)) {
            authUser.lockAccount();
            authRepository.save(authUser);

            try {
                String userName = userFeignClient.findNameByEmail(authUser.getEmail());
                mailService.sendAccountLockEmail(authUser.getEmail(), userName);
            } catch (Exception e) {
                // 로그 처리 또는 감싸기
            }
        }
    }

    private void resetLoginFailure(String email) {
        redisService.resetLoginFailCount(email);
    }

    private void authenticateUser(UserLoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void saveUserOrThrow(AuthUser user) {
        try {
            authRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new BaseException(ResponseStatus.AUTH_EMAIL_ALREADY_EXISTS);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public void checkEmailDuplicate(String email) {
        if (authRepository.existsByEmail(email)) {
            throw new BaseException(ResponseStatus.AUTH_EMAIL_ALREADY_EXISTS);
        }
    }

    @Transactional
    @Override
    public void sendVerificationCode(String email) {
        String code = mailService.generateVerificationCode();
        mailService.sendVerificationEmail(email, code);
        redisService.saveVerificationCode(email, code, EMAIL_VERIFICATION_TTL);
    }

    @Transactional
    @Override
    public void verifyEmailCode(String email, String code) {
        String storedCode = redisService.getVerificationCode(email);

        if (storedCode == null) {
            throw new BaseException(ResponseStatus.AUTH_VERIFICATION_CODE_NOT_FOUND);
        }

        if (!code.equals(storedCode)) {
            throw new BaseException(ResponseStatus.AUTH_VERIFICATION_FAILED);
        }
    }

    @Transactional
    @Override
    public SocialLoginResponseDto registerSocialUser(String accessToken, SocialRegisterRequestVo socialRegisterRequestVo) {
        String email = socialOAuthClient.getEmail("kakao", accessToken);

        AuthUser existingUser = authRepository.findByEmail(email).orElse(null);
        if (existingUser != null) {
            return generateTokensAndSave(existingUser.getUserUuid());
        }

        AuthUser newUser = registerNewSocialUser(email);
        try {
            UserRegisterRequestForUserServiceDto feignDto = UserRegisterRequestForUserServiceDto.of(
                    newUser.getUserUuid(),
                    socialRegisterRequestVo.getName(),
                    socialRegisterRequestVo.getPhoneNumber()
            );
            userFeignClient.registerUser(feignDto);
        } catch (Exception e) {
            authRepository.deleteById(newUser.getId());
            throw new BaseException(ResponseStatus.AUTH_USER_SERVICE_ERROR);
        }

        return generateTokensAndSave(newUser.getUserUuid());
    }

    private AuthUser registerNewSocialUser(String email) {
        String userUuid = UUIDGenerator.generateUUID();

        AuthUser user = AuthUser.builder()
                .userUuid(userUuid)
                .email(email)
                .loginType(LoginType.SOCIAL)
                .provider(SocialProvider.KAKAO)
                .build();

        try {
            return authRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new BaseException(ResponseStatus.AUTH_EMAIL_ALREADY_EXISTS);
        }
    }

    private SocialLoginResponseDto generateTokensAndSave(String userUuid) {
        String accessToken = jwtProvider.generateAccessToken(userUuid);
        String refreshToken = jwtProvider.generateRefreshToken(userUuid);
        redisService.saveRefreshToken(userUuid, refreshToken, REFRESH_TOKEN_EXPIRY_MILLIS);

        return SocialLoginResponseDto.of(userUuid, accessToken, refreshToken);
    }
}

