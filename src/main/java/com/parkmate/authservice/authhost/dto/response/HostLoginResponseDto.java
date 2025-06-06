package com.parkmate.authservice.authhost.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HostLoginResponseDto {

    private String accessToken;
    private String refreshToken;
    private String hostUuid;

    @Builder
    private HostLoginResponseDto(String accessToken, String refreshToken, String hostUuid) {

        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.hostUuid = hostUuid;
    }

    public static HostLoginResponseDto of(String hostUuid, String accessToken, String refreshToken) {
        return HostLoginResponseDto.builder()
                .hostUuid(hostUuid)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
