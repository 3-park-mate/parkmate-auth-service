package com.parkmate.authservice.authuser.dto.request;

import com.parkmate.authservice.authuser.vo.request.SocialLoginRequestVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialLoginRequestDto {

    private String provider;
    private String accessToken;

    @Builder
    private SocialLoginRequestDto(String provider, String accessToken) {
        this.provider = provider;
        this.accessToken = accessToken;
    }

    public static SocialLoginRequestDto from(SocialLoginRequestVo socialLoginRequestVo) {
        return SocialLoginRequestDto.builder()
                .provider(socialLoginRequestVo.getProvider())
                .accessToken(socialLoginRequestVo.getAccessToken())
                .build();
    }
}