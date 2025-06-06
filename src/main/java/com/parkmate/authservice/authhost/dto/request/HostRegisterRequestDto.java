package com.parkmate.authservice.authhost.dto.request;

import com.parkmate.authservice.authhost.domain.AuthHost;
import com.parkmate.authservice.authhost.vo.request.HostRegisterRequestVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@NoArgsConstructor
public class HostRegisterRequestDto {

    private String email;
    private String password;

    @Builder
    private HostRegisterRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public static HostRegisterRequestDto from(HostRegisterRequestVo vo) {
        return HostRegisterRequestDto.builder()
                .email(vo.getEmail())
                .password(vo.getPassword())
                .build();
    }

    public AuthHost toEntity(String hostUuid, PasswordEncoder passwordEncoder) {
        return AuthHost.builder()
                .hostUuid(hostUuid)
                .email(this.email)
                .password(passwordEncoder.encode(this.password))
                .build();
    }
}