package com.parkmate.authservice.authhost.vo.response;

import com.parkmate.authservice.authhost.dto.response.HostLoginResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HostLoginResponseVo {

    private String accessToken;
    private String refreshToken;
    private String hostUuid;

    @Builder
    private HostLoginResponseVo(String accessToken, String refreshToken, String hostUuid) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.hostUuid = hostUuid;
    }

    public static HostLoginResponseVo from(HostLoginResponseDto hostLoginResponseDto) {
        return HostLoginResponseVo.builder()
                .accessToken(hostLoginResponseDto.getAccessToken())
                .refreshToken(hostLoginResponseDto.getRefreshToken())
                .hostUuid(hostLoginResponseDto.getHostUuid())
                .build();
    }
}
