package com.parkmate.authservice.authuser.infrastructure.client;

import com.parkmate.authservice.authuser.domain.SocialProvider;
import com.parkmate.authservice.common.exception.BaseException;
import com.parkmate.authservice.common.response.ResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocialOAuthClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${oauth.kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    public String getEmail(SocialProvider provider, String accessToken) {

        if (provider != SocialProvider.KAKAO) {
            throw new BaseException(ResponseStatus.AUTH_SOCIAL_PROVIDER_NOT_SUPPORTED);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    kakaoUserInfoUri,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            Map<String, Object> kakaoAccount = (Map<String, Object>) response.getBody().get("kakao_account");

            if (kakaoAccount == null || !Boolean.TRUE.equals(kakaoAccount.get("has_email"))) {
                throw new BaseException(ResponseStatus.AUTH_VERIFICATION_FAILED);
            }

            return (String) kakaoAccount.get("email");

        } catch (Exception e) {
            log.error("카카오 사용자 정보 요청 실패", e);
            throw new BaseException(ResponseStatus.AUTH_USER_SERVICE_ERROR);
        }
    }
}