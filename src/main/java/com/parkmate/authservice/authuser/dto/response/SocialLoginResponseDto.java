package com.parkmate.authservice.authuser.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialLoginResponseDto {

    private String userUuid;
    private String accessToken;
    private String refreshToken;

    @Builder
    private SocialLoginResponseDto(String userUuid,
                                   String accessToken,
                                   String refreshToken) {

        this.userUuid = userUuid;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static SocialLoginResponseDto of(String userUuid,
                                            String accessToken,
                                            String refreshToken) {

        return SocialLoginResponseDto.builder()
                .userUuid(userUuid)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}