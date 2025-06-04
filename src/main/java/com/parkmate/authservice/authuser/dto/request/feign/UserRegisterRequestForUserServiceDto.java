package com.parkmate.authservice.authuser.dto.request.feign;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserRegisterRequestForUserServiceDto {

    private String userUuid;
    private String name;
    private String phoneNumber;

    @Builder
    private UserRegisterRequestForUserServiceDto(String userUuid,
                                                 String name,
                                                 String phoneNumber
                                                 ) {

        this.userUuid = userUuid;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public static UserRegisterRequestForUserServiceDto of(String userUuid,
                                                          String name,
                                                          String phoneNumber
                                                          ) {

        return UserRegisterRequestForUserServiceDto.builder()
                .userUuid(userUuid)
                .name(name)
                .phoneNumber(phoneNumber)
                .build();
    }
}