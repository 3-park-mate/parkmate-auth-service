package com.parkmate.authservice.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "auth.login.fail")
public class LoginFailPolicyProperties {

    private int limit = 5;
}
