package com.parkmate.authservice.authuser.dto.request;

import com.parkmate.authservice.authuser.domain.AuthUser;
import com.parkmate.authservice.authuser.domain.LoginType;
import com.parkmate.authservice.authuser.domain.SocialProvider;
import com.parkmate.authservice.authuser.vo.request.UserRegisterRequestVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@NoArgsConstructor
public class UserRegisterRequestDto {

    private String email;
    private String password;

    @Builder
    private UserRegisterRequestDto(String email,
                                   String password) {
        this.email = email;
        this.password = password;
    }

    public static UserRegisterRequestDto from(UserRegisterRequestVo userRegisterRequestVo) {

        return UserRegisterRequestDto.builder()
                .email(userRegisterRequestVo.getEmail())
                .password(userRegisterRequestVo.getPassword())
                .build();
    }

    public AuthUser toEntity(String userUuid, PasswordEncoder encodedPassword) {

        return AuthUser.builder()
                .userUuid(userUuid)
                .email(this.email)
                .password(encodedPassword.encode(this.password))
                .loginType(LoginType.NORMAL)
                .socialProvider(SocialProvider.NONE)
                .build();
    }
}
