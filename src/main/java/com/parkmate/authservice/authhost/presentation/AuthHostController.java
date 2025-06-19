package com.parkmate.authservice.authhost.presentation;

import com.parkmate.authservice.authuser.vo.request.EmailDuplicateCheckRequestVo;
import com.parkmate.authservice.authuser.vo.request.VerifyEmailCodeRequestVo;
import com.parkmate.authservice.authuser.vo.response.EmailDuplicateResponseVo;
import com.parkmate.authservice.authuser.vo.response.VerifyEmailCodeResponseVo;
import com.parkmate.authservice.common.response.ApiResponse;
import com.parkmate.authservice.authhost.application.AuthHostService;
import com.parkmate.authservice.authhost.dto.request.HostLoginRequestDto;
import com.parkmate.authservice.authhost.dto.response.HostLoginResponseDto;
import com.parkmate.authservice.authhost.vo.request.HostLoginRequestVo;
import com.parkmate.authservice.authhost.vo.request.HostRegisterRequestVo;
import com.parkmate.authservice.authhost.vo.response.HostLoginResponseVo;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/host")
@RequiredArgsConstructor
public class AuthHostController {

    private final AuthHostService authHostService;

    @Operation(
            summary = "호스트 로그인",
            description = "호스트 계정으로 로그인하는 API입니다. 이메일과 비밀번호를 입력받아 JWT AccessToken과 RefreshToken을 반환합니다.",
            tags = {"AUTH-HOST-SERVICE"}
    )
    @PostMapping("/login")
    public ApiResponse<HostLoginResponseVo> login(@Valid @RequestBody HostLoginRequestVo hostLoginRequestVo) {

        HostLoginRequestDto requestDto = HostLoginRequestDto.from(hostLoginRequestVo);
        HostLoginResponseDto responseDto = authHostService.login(requestDto);

        return ApiResponse.of(
                HttpStatus.OK,
                "호스트 로그인에 성공했습니다.",
                HostLoginResponseVo.from(responseDto)
        );
    }

    @Operation(
            summary = "호스트 로그아웃",
            description = "호스트 계정으로 로그아웃하는 API입니다. RefreshToken을 삭제합니다.",
            tags = {"AUTH-HOST-SERVICE"}
    )
    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader("X-Host-UUID") String hostUuid) {

        authHostService.logout(hostUuid);
        return ApiResponse.of(
                HttpStatus.RESET_CONTENT,
                "호스트 로그아웃 되었습니다."
        );
    }

    @Operation(
            summary = "호스트 회원가입",
            description = "호스트 계정을 회원가입하는 API입니다. 이메일, 비밀번호, 이름, 전화번호, 계좌번호, 사업자등록번호, 정산주기를 입력받습니다.",
            tags = {"AUTH-HOST-SERVICE"}
    )
    @PostMapping("/register")
    public ApiResponse<String> register(@Valid @RequestBody HostRegisterRequestVo hostRegisterRequestVo) {

        log.info("받은 이메일: '{}'", hostRegisterRequestVo.getEmail());
        log.info("받은 인증코드: '{}'", hostRegisterRequestVo.getVerificationCode());

        authHostService.register(hostRegisterRequestVo);
        return ApiResponse.of(
                HttpStatus.CREATED,
                "호스트 회원가입이 완료되었습니다."
        );
    }

    @Operation(
            summary = "호스트 이메일 중복 확인",
            description = "호스트의 이메일이 중복되었는지 확인합니다. (boolean 으로 반환)",
            tags = {"AUTH-HOST-SERVICE"}
    )
    @PostMapping("/checkEmail")
    public ApiResponse<EmailDuplicateResponseVo> checkEmailDuplicate(@RequestBody EmailDuplicateCheckRequestVo emailDuplicateCheckRequestVo) {

        boolean isDuplicate = authHostService.isEmailDuplicate(emailDuplicateCheckRequestVo.getEmail());

        return ApiResponse.of(
                HttpStatus.OK,
                "호스트 이메일 중복 여부 확인 성공",
                EmailDuplicateResponseVo.of(isDuplicate)
        );
    }

    @Operation(
            summary = "이메일 인증코드 발송",
            description = "사용자 이메일로 인증코드를 발송합니다.",
            tags = {"AUTH-HOST-SERVICE"}
    )
    @PostMapping("/sendVerificationCode")
    public ApiResponse<String> sendVerificationCode(@RequestParam String email) {

        authHostService.sendVerificationCode(email);
        return ApiResponse.of(
                HttpStatus.OK,
                "인증 코드가 이메일로 전송되었습니다."
        );
    }

    @Operation(
            summary = "이메일 인증코드 검증",
            description = """
        사용자가 입력한 이메일 인증코드를 검증합니다. <br><br>
        🔐 인증 실패 시 다음과 같은 제한이 적용됩니다: <br>
        - 인증 코드 5회 실패 시 10분간 인증 시도 차단<br>
        - 인증코드 재요청 시 실패 횟수 초기화<br><br>
        ❗ 인증 코드 유효 시간은 3분입니다.
        """,
            tags = {"AUTH-HOST-SERVICE"}
    )
    @PostMapping("/verifyCode")
    public ApiResponse<VerifyEmailCodeResponseVo> verifyEmailCode(
            @Valid @RequestBody VerifyEmailCodeRequestVo verifyEmailCodeRequestVo
    ) {
        boolean isValid = authHostService.verifyEmailCode(
                verifyEmailCodeRequestVo.getEmail(),
                verifyEmailCodeRequestVo.getVerificationCode()
        );

        return ApiResponse.of(
                HttpStatus.OK,
                "이메일 인증번호 검증 성공",
                VerifyEmailCodeResponseVo.of(isValid)
        );
    }
}