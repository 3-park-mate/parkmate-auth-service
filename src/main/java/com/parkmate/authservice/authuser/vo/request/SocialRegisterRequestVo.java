package com.parkmate.authservice.authuser.vo.request;

import com.parkmate.authservice.authuser.domain.SocialProvider;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialRegisterRequestVo {

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "이름은 필수 입력값입니다.")
    @Size(min = 2, max = 50, message = "이름은 2~50자 이내여야 합니다.")
    private String name;

    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    @Pattern(
            regexp = "^010\\d{8}$",
            message = "전화번호는 010으로 시작하는 11자리 숫자여야 합니다."
    )
    private String phoneNumber;

    @NotNull(message = "소셜 제공자는 필수 입력값입니다.")
    private SocialProvider provider;
}