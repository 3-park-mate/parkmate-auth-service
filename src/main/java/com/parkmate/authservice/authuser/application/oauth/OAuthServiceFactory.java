package com.parkmate.authservice.authuser.application.oauth;

import com.parkmate.authservice.authuser.domain.SocialProvider;
import com.parkmate.authservice.common.exception.BaseException;
import com.parkmate.authservice.common.response.ResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OAuthServiceFactory {

    private final List<OAuthService> oAuthServices;

    public OAuthService getOAuthService(SocialProvider provider) {
        return oAuthServices.stream()
                .filter(service -> service.supports(provider))
                .findFirst()
                .orElseThrow(() -> new BaseException(ResponseStatus.AUTH_SOCIAL_PROVIDER_NOT_SUPPORTED));
    }
}
