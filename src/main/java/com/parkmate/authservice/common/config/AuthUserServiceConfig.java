package com.parkmate.authservice.common.config;

import com.parkmate.authservice.authuser.application.AuthService;
import com.parkmate.authservice.authuser.application.AuthServiceImpl;
import com.parkmate.authservice.authuser.application.policy.AuthUserPolicyService;
import com.parkmate.authservice.authuser.infrastructure.AuthRepository;
import com.parkmate.authservice.authuser.infrastructure.client.SocialOAuthClient;
import com.parkmate.authservice.authuser.infrastructure.client.UserFeignClient;
import com.parkmate.authservice.common.mail.MailService;
import com.parkmate.authservice.common.redis.RedisService;
import com.parkmate.authservice.common.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthUserServiceConfig {

    @Bean
    public AuthService authUserService(
            AuthRepository authRepository,
            PasswordEncoder passwordEncoder,
            RedisService redisService,
            JwtProvider jwtProvider,
            UserFeignClient userFeignClient,
            MailService mailService,
            @Qualifier("userAuthenticationManager") AuthenticationManager authenticationManager,
            AuthUserPolicyService authUserPolicyService,
            SocialOAuthClient socialOAuthClient
    ) {
        return new AuthServiceImpl(
                authRepository,
                passwordEncoder,
                redisService,
                jwtProvider,
                userFeignClient,
                mailService,
                authenticationManager,
                authUserPolicyService,
                socialOAuthClient
        );
    }
}

