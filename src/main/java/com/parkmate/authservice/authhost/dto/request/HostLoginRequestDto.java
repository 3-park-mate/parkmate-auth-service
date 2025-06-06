package com.parkmate.authservice.authhost.dto.request;

import com.parkmate.authservice.authhost.vo.request.HostLoginRequestVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@NoArgsConstructor
public class HostLoginRequestDto {

    private String email;
    private String password;

    @Builder
    private HostLoginRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public static HostLoginRequestDto from(HostLoginRequestVo hostLoginRequestVo) {

        return HostLoginRequestDto.builder()
                .email(hostLoginRequestVo.getEmail())
                .password(hostLoginRequestVo.getPassword())
                .build();
    }

    public boolean isPasswordMatch(String encodedPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(this.password, encodedPassword);
    }
}
