package com.parkmate.authservice.authuser.application.oauth;

import com.parkmate.authservice.authuser.domain.SocialProvider;

public interface OAuthService {

    boolean supports(SocialProvider provider);
    String getEmail(String accessToken);
}