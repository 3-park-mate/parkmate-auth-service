package com.parkmate.authservice.authuser.application.policy;

import com.parkmate.authservice.authuser.domain.AuthUser;
import com.parkmate.authservice.common.config.LoginFailPolicyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthUserPolicyService {

    private final LoginFailPolicyProperties loginFailPolicyProperties;

    public boolean shouldLockAccount(int currentFailCount) {

        return currentFailCount >= loginFailPolicyProperties.getLimit();
    }

    public boolean isAccountLocked(AuthUser user) {

        return user.isAccountLocked();
    }
}