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
public class AuthHostServiceImpl implements AuthHostService {

    private final AuthHostRepository authHostRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final HostFeignClient hostFeignClient;
    private final BiznoVerificationService biznoVerificationService;

    private static final long REFRESH_TOKEN_EXPIRY_MILLIS = Duration.ofDays(7).toMillis();

    @Transactional
    @Override
    public HostLoginResponseDto login(HostLoginRequestDto hostLoginRequestDto) {

        AuthHost authHost= authHostRepository.findByEmail(hostLoginRequestDto.getEmail())
                .orElseThrow(() -> new BaseException(ResponseStatus.AUTH_HOST_NOT_FOUND));

        if (!hostLoginRequestDto.isPasswordMatch(authHost.getPassword(), passwordEncoder)) {
            throw new BaseException(ResponseStatus.INVALID_AUTH_PASSWORD);
        }

        authenticateHost(hostLoginRequestDto);

        String accessToken = jwtProvider.generateAccessToken(authHost.getHostUuid());
        String refreshToken = jwtProvider.generateRefreshToken(authHost.getHostUuid());

        redisService.saveRefreshToken(authHost.getHostUuid(), refreshToken, REFRESH_TOKEN_EXPIRY_MILLIS);

        return HostLoginResponseDto.of(authHost.getHostUuid(), accessToken, refreshToken);
    }

    @Transactional
    @Override
    public void logout(String hostUuid) {
        redisService.deleteRefreshToken(hostUuid);
    }

    @Transactional
    @Override
    public void register(HostRegisterRequestDto hostRegisterRequestDto, HostRegisterRequestVo hostRegisterRequestVo) {

        String hostUuid = UUIDGenerator.generateUUID();
        AuthHost host = hostRegisterRequestDto.toEntity(hostUuid, passwordEncoder);

        biznoVerificationService.verify(hostRegisterRequestVo.getBusinessRegistrationNumber());

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
            authHostRepository.deleteById(host.getId());
            throw new BaseException(ResponseStatus.AUTH_USER_SERVICE_ERROR);
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