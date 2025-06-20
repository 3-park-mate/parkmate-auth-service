package com.parkmate.authservice.authhost.application;

import com.parkmate.authservice.authhost.dto.request.HostLoginRequestDto;
import com.parkmate.authservice.authhost.dto.request.HostRegisterRequestDto;
import com.parkmate.authservice.authhost.dto.response.HostLoginResponseDto;
import com.parkmate.authservice.authhost.vo.request.HostRegisterRequestVo;

public interface AuthHostService {

    HostLoginResponseDto login(HostLoginRequestDto hostLoginRequestDto);

    void logout(String hostUuid);

    void register(HostRegisterRequestVo hostRegisterRequestVo);

    boolean isEmailDuplicate(String email);

    void sendVerificationCode(String email);

    boolean verifyEmailCode(String email, String code);
}
