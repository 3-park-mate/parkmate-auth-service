package com.parkmate.authservice.authuser.dto.request;

import com.parkmate.authservice.authuser.vo.request.SocialLoginRequestVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialLoginRequestDto {

    private String socialProvider;
    private String accessToken;

    @Builder
    private SocialLoginRequestDto(String socialProvider, String accessToken) {
        this.socialProvider = socialProvider;
        this.accessToken = accessToken;
    }

    public static SocialLoginRequestDto from(SocialLoginRequestVo socialLoginRequestVo) {
        return SocialLoginRequestDto.builder()
                .socialProvider(socialLoginRequestVo.getSocialProvider())
                .accessToken(socialLoginRequestVo.getAccessToken())
                .build();
    }
}