package com.parkmate.authservice.authuser.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserLoginResponseDto {

    private String accessToken;
    private String refreshToken;
    private String userUuid;

    @Builder
    private UserLoginResponseDto(String accessToken,
                                 String refreshToken,
                                 String userUuid) {

        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userUuid = userUuid;
    }

    public static UserLoginResponseDto of(String userUuid,
                                          String accessToken,
                                          String refreshToken) {
        return UserLoginResponseDto.builder()
                .userUuid(userUuid)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
