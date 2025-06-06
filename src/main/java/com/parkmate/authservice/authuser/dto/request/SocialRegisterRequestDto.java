package com.parkmate.authservice.authuser.dto.request;

import com.parkmate.authservice.authuser.domain.AuthUser;
import com.parkmate.authservice.authuser.domain.LoginType;
import com.parkmate.authservice.authuser.domain.SocialProvider;
import com.parkmate.authservice.authuser.vo.request.SocialRegisterRequestVo;
import com.parkmate.authservice.common.generator.UUIDGenerator;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialRegisterRequestDto {

    private String userUuid;
    private String email;
    private SocialProvider provider;

    @Builder
    private SocialRegisterRequestDto(String userUuid,
                                     String email,
                                     SocialProvider provider
    ) {
        this.userUuid = userUuid;
        this.email = email;
        this.provider = provider;

    }

    public static SocialRegisterRequestDto from(SocialRegisterRequestVo socialRegisterRequestVo) {

        return SocialRegisterRequestDto.builder()
                .userUuid(UUIDGenerator.generateUUID())
                .email(socialRegisterRequestVo.getEmail())
                .provider(socialRegisterRequestVo.getProvider())
                .build();
    }

    public AuthUser toEntity() {

        return AuthUser.builder()
                .userUuid(userUuid)
                .email(email)
                .loginType(LoginType.SOCIAL)
                .provider(provider)
                // .socialId(socialId) // 필요 시 AuthUser 엔티티에 추가
                .build();
    }
}