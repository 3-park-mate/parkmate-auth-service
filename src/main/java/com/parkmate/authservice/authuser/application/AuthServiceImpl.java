package com.parkmate.authservice.authuser.application;

import com.parkmate.authservice.authuser.application.oauth.OAuthService;
import com.parkmate.authservice.authuser.application.oauth.OAuthServiceFactory;
import com.parkmate.authservice.authuser.domain.AuthUser;
import com.parkmate.authservice.authuser.domain.LoginType;
import com.parkmate.authservice.authuser.domain.SocialProvider;
import com.parkmate.authservice.authuser.dto.request.UserLoginRequestDto;
import com.parkmate.authservice.authuser.dto.request.UserRegisterRequestDto;
import com.parkmate.authservice.authuser.dto.request.feign.UserRegisterRequestForSocialUserDto;
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
import com.parkmate.authservice.common.roletype.RoleType;
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

        redisService.resetLoginFailCount(user.getEmail(), RoleType.USER);
        authenticateUser(userLoginRequestDto);

        String accessToken = jwtProvider.generateAccessToken();
        String refreshToken = jwtProvider.generateRefreshToken();
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
        boolean isVerified = redisService.verifyEmailCode(
                userRegisterRequestDto.getEmail(),
                userRegisterRequestVo.getVerificationCode(),
                RoleType.USER
        );

        if (!isVerified) {
            throw new BaseException(ResponseStatus.INVALID_VERIFICATION_CODE);
        }

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
            redisService.deleteVerificationCode(userRegisterRequestDto.getEmail(), RoleType.USER);
        } catch (DataIntegrityViolationException e) {
            String message = e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : "";
            if (message.contains("UK_auth_user_email")) {
                throw new BaseException(ResponseStatus.AUTH_EMAIL_ALREADY_EXISTS);
            } else {
                throw new BaseException(ResponseStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            authRepository.deleteById(newUser.getId());
            throw new BaseException(ResponseStatus.AUTH_USER_REGISTER_FAILED);
        }
    }

    private boolean shouldLockAccount(int currentFailCount) {
        return currentFailCount >= LOGIN_FAIL_LIMIT;
    }

    private void handleFailedLogin(AuthUser authUser) {
        int failCount = redisService.incrementLoginFailCount(authUser.getEmail(), RoleType.USER);

        if (shouldLockAccount(failCount)) {
            authUser.lockAccount();
            authRepository.save(authUser);
            try {
                String userName = userFeignClient.findNameByEmail(authUser.getEmail());
                mailService.sendAccountLockEmail(authUser.getEmail(), userName);
            } catch (Exception e) {
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
        RoleType roleType = RoleType.USER;
        String existingCode = redisService.getVerificationCode(email, roleType);
        if (existingCode != null) {
            throw new BaseException(ResponseStatus.VERIFICATION_CODE_ALREADY_SENT);
        }

        String code = mailService.generateVerificationCode();
        mailService.sendVerificationEmail(email, code);
        redisService.saveVerificationCode(email, code, EMAIL_VERIFICATION_TTL, roleType);
    }

    @Transactional
    @Override
    public boolean verifyEmailCode(String email, String code) {
        RoleType roleType = RoleType.USER;
        if (redisService.isVerificationAttemptBlocked(email, roleType)) {
            throw new BaseException(ResponseStatus.VERIFICATION_ATTEMPT_BLOCKED);
        }

        boolean isVerified = redisService.verifyEmailCode(email, code, roleType);

        if (!isVerified) {
            int failCount = redisService.incrementVerificationAttemptFailCount(email, roleType);
            if (failCount > 5) {
                redisService.blockVerificationAttempts(email, roleType, Duration.ofMinutes(10));
                throw new BaseException(ResponseStatus.VERIFICATION_ATTEMPT_BLOCKED);
            }
            throw new BaseException(ResponseStatus.INVALID_VERIFICATION_CODE_MISMATCH);
        }

        redisService.resetVerificationAttemptFailCount(email, roleType);
        return true;
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
                UserRegisterRequestForSocialUserDto feignDto = UserRegisterRequestForSocialUserDto.of(
                        newUser.getUserUuid(),
                        socialRegisterRequestVo.getName()
                );
                userFeignClient.registerSocialUser(feignDto);
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

    @Transactional
    @Override
    public String getEmailByUserUuid(String userUuid) {
        AuthUser authUser = authRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new BaseException(ResponseStatus.AUTH_USER_NOT_FOUND));
        return authUser.getEmail();
    }
}

