package com.parkmate.authservice.authuser.dto.request;

import com.parkmate.authservice.authuser.vo.request.UserLoginRequestVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserLoginRequestDto {

    private String email;
    private String password;

    @Builder
    private UserLoginRequestDto(String email, String password) {

        this.email = email;
        this.password = password;
    }
    public static UserLoginRequestDto from(UserLoginRequestVo userLoginRequestVo) {

        return UserLoginRequestDto.builder()
                .email(userLoginRequestVo.getEmail())
                .password(userLoginRequestVo.getPassword())
                .build();
    }
}

