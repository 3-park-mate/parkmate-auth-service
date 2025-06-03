package com.parkmate.authservice.authuser.dto.request.feign;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserRegisterRequestForUserServiceDto {

    private String userUuid;
    private String email;
    private String name;
    private String phoneNumber;

    @Builder
    private UserRegisterRequestForUserServiceDto(String userUuid,
                                                 String email,
                                                 String name,
                                                 String phoneNumber
                                                 ) {

        this.userUuid = userUuid;
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public static UserRegisterRequestForUserServiceDto of(String userUuid,
                                                          String email,
                                                          String name,
                                                          String phoneNumber
                                                          ) {

        return UserRegisterRequestForUserServiceDto.builder()
                .userUuid(userUuid)
                .email(email)
                .name(name)
                .phoneNumber(phoneNumber)
                .build();
    }
}