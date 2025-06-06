package com.parkmate.authservice.common.config;

import com.parkmate.authservice.authhost.application.AuthHostService;
import com.parkmate.authservice.authhost.application.AuthHostServiceImpl;
import com.parkmate.authservice.authhost.application.BiznoVerificationService;
import com.parkmate.authservice.authhost.infrastructure.AuthHostRepository;
import com.parkmate.authservice.authhost.infrastructure.client.HostFeignClient;
import com.parkmate.authservice.common.redis.RedisService;
import com.parkmate.authservice.common.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthHostServiceConfig {

    @Bean
    public AuthHostService authHostService(
            AuthHostRepository authHostRepository,
            PasswordEncoder passwordEncoder,
            RedisService redisService,
            JwtProvider jwtProvider,
            @Qualifier("hostAuthenticationManager") AuthenticationManager authenticationManager,
            HostFeignClient hostFeignClient,
            BiznoVerificationService biznoVerificationService
    ) {
        return new AuthHostServiceImpl(
                authHostRepository,
                passwordEncoder,
                redisService,
                jwtProvider,
                authenticationManager,
                hostFeignClient,
                biznoVerificationService
        );
    }
}