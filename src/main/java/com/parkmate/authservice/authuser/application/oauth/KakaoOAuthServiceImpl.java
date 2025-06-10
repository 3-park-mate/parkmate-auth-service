package com.parkmate.authservice.authuser.application.oauth;

import com.parkmate.authservice.authuser.domain.SocialProvider;
import com.parkmate.authservice.authuser.infrastructure.client.SocialOAuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoOAuthServiceImpl implements OAuthService {

    private final SocialOAuthClient socialOAuthClient;

    @Override
    public boolean supports(SocialProvider provider) {
        return provider == SocialProvider.KAKAO;
    }

    @Override
    public String getEmail(String accessToken) {
        return socialOAuthClient.getEmail(SocialProvider.KAKAO, accessToken);
    }
}