package com.parkmate.authservice.authuser.vo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialLoginRequestVo {

    @NotBlank(message = "소셜 로그인 제공자는 필수입니다. (예: kakao)")
    private String provider;

    @NotBlank(message = "소셜 액세스 토큰은 필수입니다.")
    private String accessToken;
}