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

@Service
public class AuthHostServiceImpl implements AuthHostService {

    private final AuthHostRepository authHostRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final HostFeignClient hostFeignClient;
    private final BiznoVerificationService biznoVerificationService;

    private static final long REFRESH_TOKEN_EXPIRY_MILLIS = Duration.ofDays(7).toMillis();
    private static final int LOGIN_FAIL_LIMIT = 5;

    public AuthHostServiceImpl(
            AuthHostRepository authHostRepository,
            PasswordEncoder passwordEncoder,
            RedisService redisService,
            JwtProvider jwtProvider,
            HostFeignClient hostFeignClient,
            BiznoVerificationService biznoVerificationService,
            @Qualifier("hostAuthenticationManager") AuthenticationManager authenticationManager
    ) {
        this.authHostRepository = authHostRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisService = redisService;
        this.jwtProvider = jwtProvider;
        this.hostFeignClient = hostFeignClient;
        this.biznoVerificationService = biznoVerificationService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    @Override
    public HostLoginResponseDto login(HostLoginRequestDto hostLoginRequestDto) {

        AuthHost authHost= authHostRepository.findByEmail(hostLoginRequestDto.getEmail())
                .orElseThrow(() -> new BaseException(ResponseStatus.AUTH_HOST_NOT_FOUND));

        if (authHost.isAccountLocked()) {
            throw new BaseException(ResponseStatus.AUTH_ACCOUNT_LOCKED);
        }

        if (!hostLoginRequestDto.isPasswordMatch(authHost.getPassword(), passwordEncoder)) {
            handleFailedLogin(authHost);
            throw new BaseException(ResponseStatus.INVALID_AUTH_PASSWORD);
        }

        redisService.resetHostLoginFailCount(authHost.getEmail());

        authenticateHost(hostLoginRequestDto);

        String accessToken = jwtProvider.generateAccessToken();
        String refreshToken = jwtProvider.generateRefreshToken();

        redisService.saveRefreshToken(authHost.getHostUuid(), refreshToken, REFRESH_TOKEN_EXPIRY_MILLIS);

        return HostLoginResponseDto.of(authHost.getHostUuid(), accessToken, refreshToken);
    }

    private void handleFailedLogin(AuthHost authHost) {
        int failCount = redisService.incrementHostLoginFailCount(authHost.getEmail());

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
        } catch (Exception e) {
            // Rollback 처리
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
}