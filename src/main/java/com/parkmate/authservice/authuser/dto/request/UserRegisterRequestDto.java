package com.parkmate.authservice.authuser.dto.request;

import com.parkmate.authservice.authuser.domain.AuthUser;
import com.parkmate.authservice.authuser.vo.request.UserRegisterRequestVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@NoArgsConstructor
public class UserRegisterRequestDto {

    private String email;
    private String name;
    private String password;
    private String phoneNumber;
    private String verificationCode;

    @Builder
    private UserRegisterRequestDto(String email,
                                   String name,
                                   String password,
                                   String phoneNumber,
                                   String verificationCode) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.verificationCode = verificationCode;
    }

    public static UserRegisterRequestDto from(UserRegisterRequestVo userRegisterRequestVo) {

        return UserRegisterRequestDto.builder()
                .email(userRegisterRequestVo.getEmail())
                .name(userRegisterRequestVo.getName())
                .password(userRegisterRequestVo.getPassword())
                .phoneNumber(userRegisterRequestVo.getPhoneNumber())
                .verificationCode(userRegisterRequestVo.getVerificationCode())
                .build();
    }

    public AuthUser toEntity(String userUuid, PasswordEncoder encodedPassword) {

        return AuthUser.builder()
                .userUuid(userUuid)
                .email(this.email)
                .password(encodedPassword.encode(this.password))
                .build();
    }
}
