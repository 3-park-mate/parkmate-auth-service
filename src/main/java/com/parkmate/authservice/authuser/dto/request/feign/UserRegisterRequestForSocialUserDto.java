package com.parkmate.authservice.authuser.dto.request.feign;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserRegisterRequestForSocialUserDto {

    private String userUuid;
    private String name;

    @Builder
    private UserRegisterRequestForSocialUserDto(String userUuid,
                                                       String name) {
        this.userUuid = userUuid;
        this.name = name;
    }

    public static UserRegisterRequestForSocialUserDto of(String userUuid,
                                                                String name) {
        return UserRegisterRequestForSocialUserDto.builder()
                .userUuid(userUuid)
                .name(name)
                .build();
    }
}
