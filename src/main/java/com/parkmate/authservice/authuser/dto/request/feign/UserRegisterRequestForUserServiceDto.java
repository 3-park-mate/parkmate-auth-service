package com.parkmate.authservice.authuser.dto.request.feign;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(force = true)
public class UserRegisterRequestForUserServiceDto {

    private String userUuid;
    private String email;
    private String name;
    private String phoneNumber;
    private Integer point;

    @Builder
    private UserRegisterRequestForUserServiceDto(String userUuid, String email,
                                                 String name, String phoneNumber, Integer point) {
        this.userUuid = userUuid;
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.point = point;
    }

    public static UserRegisterRequestForUserServiceDto of(String userUuid, String email,
                                                          String name, String phoneNumber, Integer point) {
        return UserRegisterRequestForUserServiceDto.builder()
                .userUuid(userUuid)
                .email(email)
                .name(name)
                .phoneNumber(phoneNumber)
                .point(point)
                .build();
    }
}