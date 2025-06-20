package com.parkmate.authservice.authhost.application;

import com.parkmate.authservice.authhost.domain.AuthHost;
import com.parkmate.authservice.authhost.dto.request.HostLoginRequestDto;
import com.parkmate.authservice.authhost.dto.request.HostRegisterRequestDto;
import com.parkmate.authservice.authhost.dto.request.feign.HostRegisterRequestForHostServiceDto;
import com.parkmate.authservice.authhost.dto.response.HostLoginResponseDto;
import com.parkmate.authservice.authhost.infrastructure.AuthHostRepository;
import com.parkmate.authservice.authhost.infrastructure.client.HostFeignClient;
import com.parkmate.authservice.authhost.vo.request.HostRegisterRequestVo;
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

@Service
public class AuthHostServiceImpl implements AuthHostService {

    private final AuthHostRepository authHostRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final HostFeignClient hostFeignClient;
    private final BiznoVerificationService biznoVerificationService;
    private final MailService mailService;

    private static final long REFRESH_TOKEN_EXPIRY_MILLIS = Duration.ofDays(7).toMillis();
    private static final int LOGIN_FAIL_LIMIT = 5;

    public AuthHostServiceImpl(
            AuthHostRepository authHostRepository,
            PasswordEncoder passwordEncoder,
            RedisService redisService,
            JwtProvider jwtProvider,
            HostFeignClient hostFeignClient,
            BiznoVerificationService biznoVerificationService,
            @Qualifier("hostAuthenticationManager") AuthenticationManager authenticationManager,
            MailService mailService) {
        this.authHostRepository = authHostRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisService = redisService;
        this.jwtProvider = jwtProvider;
        this.hostFeignClient = hostFeignClient;
        this.biznoVerificationService = biznoVerificationService;
        this.authenticationManager = authenticationManager;
        this.mailService = mailService;
    }

    @Transactional
    @Override
    public HostLoginResponseDto login(HostLoginRequestDto hostLoginRequestDto) {

        AuthHost authHost = authHostRepository.findByEmail(hostLoginRequestDto.getEmail())
                .orElseThrow(() -> new BaseException(ResponseStatus.AUTH_HOST_NOT_FOUND));

        if (authHost.isAccountLocked()) {
            throw new BaseException(ResponseStatus.AUTH_ACCOUNT_LOCKED);
        }

        if (!hostLoginRequestDto.isPasswordMatch(authHost.getPassword(), passwordEncoder)) {
            handleFailedLogin(authHost);
            throw new BaseException(ResponseStatus.INVALID_AUTH_PASSWORD);
        }

        redisService.resetLoginFailCount(authHost.getEmail(), RoleType.HOST);

        authenticateHost(hostLoginRequestDto);

        String accessToken = jwtProvider.generateAccessToken();
        String refreshToken = jwtProvider.generateRefreshToken();

        redisService.saveRefreshToken(authHost.getHostUuid(), refreshToken, REFRESH_TOKEN_EXPIRY_MILLIS);

        return HostLoginResponseDto.of(authHost.getHostUuid(), accessToken, refreshToken);
    }

    private void handleFailedLogin(AuthHost authHost) {
        int failCount = redisService.incrementLoginFailCount(authHost.getEmail(), RoleType.HOST);

        if (shouldLockAccount(failCount)) {
            authHost.lockAccount();
            authHostRepository.save(authHost);
            throw new BaseException(ResponseStatus.AUTH_ACCOUNT_LOCKED);
        }
    }

    private boolean shouldLockAccount(int currentFailCount) {
        return currentFailCount >= LOGIN_FAIL_LIMIT;
    }

    @Transactional
    @Override
    public void logout(String hostUuid) {
        redisService.deleteRefreshToken(hostUuid);
    }

    @Transactional
    @Override
    public void register(HostRegisterRequestVo hostRegisterRequestVo) {

        int cycle = hostRegisterRequestVo.getSettlementCycle();
        if (cycle != 15 && cycle != 30) {
            throw new BaseException(ResponseStatus.INVALID_SETTLEMENT_CYCLE);
        }

        boolean isVerified = redisService.verifyEmailCode(
                hostRegisterRequestVo.getEmail(),
                hostRegisterRequestVo.getVerificationCode(),
                RoleType.HOST
        );

        if (!isVerified) {
            throw new BaseException(ResponseStatus.INVALID_VERIFICATION_CODE);
        }

        biznoVerificationService.verify(hostRegisterRequestVo.getBusinessRegistrationNumber());

        String hostUuid = UUIDGenerator.generateUUID();

        HostRegisterRequestDto hostRegisterRequestDto = HostRegisterRequestDto.from(hostRegisterRequestVo);
        AuthHost host = hostRegisterRequestDto.toEntity(hostUuid, passwordEncoder);

        try {
            authHostRepository.save(host);
        } catch (DataIntegrityViolationException e) {
            throw new BaseException(ResponseStatus.AUTH_EMAIL_ALREADY_EXISTS);
        }

        try {
            HostRegisterRequestForHostServiceDto feignDto = HostRegisterRequestForHostServiceDto.of(
                    hostUuid,
                    hostRegisterRequestVo
            );

            hostFeignClient.registerHost(feignDto);

            redisService.deleteVerificationCode(hostRegisterRequestVo.getEmail(), RoleType.HOST);

        } catch (Exception e) {
            authHostRepository.deleteById(host.getId());
            throw new BaseException(ResponseStatus.AUTH_HOST_SERVICE_ERROR);
        }
    }

    private void authenticateHost(HostLoginRequestDto hostLoginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        hostLoginRequestDto.getEmail(), hostLoginRequestDto.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isEmailDuplicate(String email) {
        return authHostRepository.existsByEmail(email);
    }

    @Transactional
    @Override
    public void sendVerificationCode(String email) {

        RoleType roleType = RoleType.HOST;

        String existingCode = redisService.getVerificationCode(email, roleType);
        if (existingCode != null) {
            throw new BaseException(ResponseStatus.VERIFICATION_CODE_ALREADY_SENT);
        }

        String code = mailService.generateVerificationCode();
        mailService.sendVerificationEmail(email, code);
        redisService.saveVerificationCode(email, code, Duration.ofMinutes(3), roleType);
    }

    @Override
    public boolean verifyEmailCode(String email, String code) {

        if (redisService.isVerificationAttemptBlocked(email, RoleType.HOST)) {
            throw new BaseException(ResponseStatus.VERIFICATION_ATTEMPT_BLOCKED);
        }

        boolean isVerified = redisService.verifyEmailCode(email, code, RoleType.HOST);

        if (!isVerified) {
            int failCount = redisService.incrementVerificationAttemptFailCount(email, RoleType.HOST);
            if (failCount > 5) {
                redisService.blockVerificationAttempts(email, RoleType.HOST, Duration.ofMinutes(10));
                throw new BaseException(ResponseStatus.VERIFICATION_ATTEMPT_BLOCKED);
            }
            throw new BaseException(ResponseStatus.INVALID_VERIFICATION_CODE_MISMATCH);
        }

        redisService.resetVerificationAttemptFailCount(email, RoleType.HOST);
        return true;
    }
}