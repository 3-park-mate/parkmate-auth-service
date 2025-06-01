package com.parkmate.authservice.authuser.vo.response;

import com.parkmate.authservice.authuser.dto.response.UserLoginResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserLoginResponseVo {

    private String accessToken;
    private String refreshToken;
    private String userUuid;


    @Builder
    private UserLoginResponseVo(String accessToken, String refreshToken, String userUuid) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userUuid = userUuid;
    }

    public static UserLoginResponseVo from(UserLoginResponseDto userLoginResponseDto) {
        return UserLoginResponseVo.builder()
                .accessToken(userLoginResponseDto.getAccessToken())
                .refreshToken(userLoginResponseDto.getRefreshToken())
                .userUuid(userLoginResponseDto.getUserUuid())
                .build();
    }
}

