package com.parkmate.authservice.authhost.presentation;

import com.parkmate.authservice.common.response.ApiResponse;
import com.parkmate.authservice.authhost.application.AuthHostService;
import com.parkmate.authservice.authhost.dto.request.HostLoginRequestDto;
import com.parkmate.authservice.authhost.dto.request.HostRegisterRequestDto;
import com.parkmate.authservice.authhost.dto.response.HostLoginResponseDto;
import com.parkmate.authservice.authhost.vo.request.HostLoginRequestVo;
import com.parkmate.authservice.authhost.vo.request.HostRegisterRequestVo;
import com.parkmate.authservice.authhost.vo.response.HostLoginResponseVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthHostController {

    private final AuthHostService authHostService;

    @PostMapping("/login/host")
    public ApiResponse<HostLoginResponseVo> login(@RequestBody HostLoginRequestVo hostLoginRequestVo) {

        HostLoginRequestDto requestDto = HostLoginRequestDto.from(hostLoginRequestVo);
        HostLoginResponseDto responseDto = authHostService.login(requestDto);

        return ApiResponse.of(
                HttpStatus.OK,
                "로그인에 성공했습니다.",
                HostLoginResponseVo.from(responseDto)
        );
    }

    @PostMapping("/logout/host")
    public ApiResponse<String> logout(@RequestParam("hostuuid") String hostUuid) {

        authHostService.logout(hostUuid);
        return ApiResponse.of(
                HttpStatus.RESET_CONTENT,
                "로그아웃 되었습니다."
        );
    }

    @PostMapping("/register/host")
    public ApiResponse<String> register(@Valid @RequestBody HostRegisterRequestVo hostRegisterRequestVo) {

        HostRegisterRequestDto requestDto = HostRegisterRequestDto.from(hostRegisterRequestVo);
        authHostService.register(requestDto, hostRegisterRequestVo);

        return ApiResponse.of(
                HttpStatus.CREATED,
                "회원가입 되었습니다."
        );
    }
}