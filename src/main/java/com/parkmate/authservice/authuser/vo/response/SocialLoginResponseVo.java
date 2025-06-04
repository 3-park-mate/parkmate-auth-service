package com.parkmate.authservice.authuser.vo.response;

import com.parkmate.authservice.authuser.dto.response.SocialLoginResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialLoginResponseVo {

    private String userUuid;
    private String accessToken;
    private String refreshToken;

    @Builder
    private SocialLoginResponseVo(String userUuid,
                                  String accessToken,
                                  String refreshToken) {

        this.userUuid = userUuid;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static SocialLoginResponseVo from(SocialLoginResponseDto socialLoginResponseDto) {

        return SocialLoginResponseVo.builder()
                .userUuid(socialLoginResponseDto.getUserUuid())
                .accessToken(socialLoginResponseDto.getAccessToken())
                .refreshToken(socialLoginResponseDto.getRefreshToken())
                .build();
    }
}