package com.parkmate.authservice.authuser.application;

import com.parkmate.authservice.authuser.application.oauth.OAuthService;
import com.parkmate.authservice.authuser.application.oauth.OAuthServiceFactory;
import com.parkmate.authservice.authuser.domain.AuthUser;
import com.parkmate.authservice.authuser.domain.LoginType;
import com.parkmate.authservice.authuser.domain.SocialProvider;
import com.parkmate.authservice.authuser.dto.request.UserLoginRequestDto;
import com.parkmate.authservice.authuser.dto.request.UserRegisterRequestDto;
import com.parkmate.authservice.authuser.dto.request.feign.UserRegisterRequestForUserServiceDto;
import com.parkmate.authservice.authuser.dto.response.SocialLoginResponseDto;
import com.parkmate.authservice.authuser.dto.response.UserLoginResponseDto;
import com.parkmate.authservice.authuser.infrastructure.AuthRepository;
import com.parkmate.authservice.authuser.infrastructure.client.UserFeignClient;
import com.parkmate.authservice.authuser.vo.request.SocialRegisterRequestVo;
import com.parkmate.authservice.authuser.vo.request.UserRegisterRequestVo;
import com.parkmate.authservice.common.exception.BaseException;
import com.parkmate.authservice.common.generator.UUIDGenerator;
import com.parkmate.authservice.common.mail.MailService;
import com.parkmate.authservice.common.redis.RedisService;
import com.parkmate.authservice.common.response.ResponseStatus;
import com.parkmate.authservice.common.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;
    private final JwtProvider jwtProvider;
    private final UserFeignClient userFeignClient;
    private final MailService mailService;
    private final OAuthServiceFactory oAuthServiceFactory;
    private final AuthenticationManager authenticationManager;


    public AuthServiceImpl(
            AuthRepository authRepository,
            PasswordEncoder passwordEncoder,
            RedisService redisService,
            JwtProvider jwtProvider,
            UserFeignClient userFeignClient,
            MailService mailService,
            OAuthServiceFactory oAuthServiceFactory,
            @Qualifier("userAuthenticationManager") AuthenticationManager authenticationManager
    ) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisService = redisService;
        this.jwtProvider = jwtProvider;
        this.userFeignClient = userFeignClient;
        this.mailService = mailService;
        this.oAuthServiceFactory = oAuthServiceFactory;
        this.authenticationManager = authenticationManager;
    }

    private static final long REFRESH_TOKEN_EXPIRY_MILLIS = Duration.ofDays(7).toMillis();
    private static final Duration EMAIL_VERIFICATION_TTL = Duration.ofMinutes(3);
    private static final int LOGIN_FAIL_LIMIT = 5;

    @Transactional
    @Override
    public UserLoginResponseDto login(UserLoginRequestDto userLoginRequestDto) {

        AuthUser user = authRepository.findByEmail(userLoginRequestDto.getEmail())
                .orElseThrow(() -> new BaseException(ResponseStatus.AUTH_USER_NOT_FOUND));

        if (user.isAccountLocked()) {
            throw new BaseException(ResponseStatus.AUTH_ACCOUNT_LOCKED);
        }

        if (!userLoginRequestDto.isPasswordMatch(user.getPassword(), passwordEncoder)) {
            handleFailedLogin(user);
            throw new BaseException(ResponseStatus.INVALID_AUTH_PASSWORD);
        }

        redisService.resetUserLoginFailCount(user.getEmail());

        authenticateUser(userLoginRequestDto);

        String accessToken = jwtProvider.generateAccessToken();
        String refreshToken = jwtProvider.generateRefreshToken();

        redisService.saveRefreshToken(user.getUserUuid(), refreshToken, REFRESH_TOKEN_EXPIRY_MILLIS);

        return UserLoginResponseDto.of(user.getUserUuid(), accessToken, refreshToken);
    }

    private boolean isAccountLocked(AuthUser user) {
        return user.isAccountLocked();
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

        try {
            authRepository.save(newUser);

            UserRegisterRequestForUserServiceDto dto = UserRegisterRequestForUserServiceDto.of(
                    userUuid,
                    userRegisterRequestVo.getName(),
                    userRegisterRequestVo.getPhoneNumber()
            );

            userFeignClient.registerUser(dto);

            redisService.deleteVerificationCode(userRegisterRequestDto.getEmail());

        } catch (DataIntegrityViolationException e) {

            String message = e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : "";
            if (message.contains("UK_auth_user_email")) {
                throw new BaseException(ResponseStatus.AUTH_EMAIL_ALREADY_EXISTS);
            } else if (message.contains("UK_auth_user_userUuid")) {
                throw new BaseException(ResponseStatus.AUTH_USER_UUID_ALREADY_EXISTS);
            } else {
                throw new BaseException(ResponseStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {

            authRepository.deleteById(newUser.getId());
            throw new BaseException(ResponseStatus.AUTH_USER_REGISTER_FAILED);
        }
    }
    // 🔹 분리된 유틸성 메서드들
    private boolean shouldLockAccount(int currentFailCount) {
        return currentFailCount >= LOGIN_FAIL_LIMIT;
    }

    private void handleFailedLogin(AuthUser authUser) {
        int failCount = redisService.incrementUserLoginFailCount(authUser.getEmail());

        if (shouldLockAccount(failCount)) {
            authUser.lockAccount();
            authRepository.save(authUser);

            try {
                String userName = userFeignClient.findNameByEmail(authUser.getEmail());
                mailService.sendAccountLockEmail(authUser.getEmail(), userName);
            } catch (Exception e) {
                // 로그 처리 또는 감싸기
                throw new BaseException(ResponseStatus.AUTH_LOCK_MAIL_FAILED);
            }
            throw new BaseException(ResponseStatus.AUTH_ACCOUNT_LOCKED);
        }
    }

    private void authenticateUser(UserLoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isEmailDuplicate(String email) {
        return authRepository.existsByEmail(email);
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
    public boolean verifyEmailCode(String email, String code) {

        return redisService.verifyEmailCode(email, code);
    }

    @Transactional
    @Override
    public SocialLoginResponseDto registerSocialUser(String socialAccessToken, SocialRegisterRequestVo socialRegisterRequestVo) {

        OAuthService oAuthService = oAuthServiceFactory.getOAuthService(socialRegisterRequestVo.getProvider());
        String email = oAuthService.getEmail(socialAccessToken);

        Optional<AuthUser> optionalUser = authRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {

            AuthUser newUser = registerNewSocialUser(email, socialRegisterRequestVo.getProvider());

            try {
                UserRegisterRequestForUserServiceDto feignDto = UserRegisterRequestForUserServiceDto.of(
                        newUser.getUserUuid(),
                        socialRegisterRequestVo.getName(),
                        socialRegisterRequestVo.getPhoneNumber()
                );
                userFeignClient.registerUser(feignDto);
            } catch (Exception e) {
                authRepository.deleteById(newUser.getId());
                throw new BaseException(ResponseStatus.AUTH_USER_REGISTER_FAILED);
            }

            return generateTokensAndSave(newUser.getUserUuid());
        }

        return generateTokensAndSave(optionalUser.get().getUserUuid());
    }

    private AuthUser registerNewSocialUser(String email, SocialProvider socialProvider) {
        String userUuid = UUIDGenerator.generateUUID();

        AuthUser user = AuthUser.builder()
                .userUuid(userUuid)
                .email(email)
                .loginType(LoginType.SOCIAL)
                .socialProvider(socialProvider)
                .build();

        try {
            return authRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new BaseException(ResponseStatus.AUTH_EMAIL_ALREADY_EXISTS);
        }
    }

    private SocialLoginResponseDto generateTokensAndSave(String userUuid) {

        String accessToken = jwtProvider.generateAccessToken();
        String refreshToken = jwtProvider.generateRefreshToken();

        redisService.saveRefreshToken(userUuid, refreshToken, REFRESH_TOKEN_EXPIRY_MILLIS);

        return SocialLoginResponseDto.of(userUuid, accessToken, refreshToken);
    }
}

