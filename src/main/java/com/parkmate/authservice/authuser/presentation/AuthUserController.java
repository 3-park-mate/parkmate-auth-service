package com.parkmate.authservice.authuser.presentation;

import com.parkmate.authservice.authuser.application.AuthService;
import com.parkmate.authservice.authuser.dto.request.UserLoginRequestDto;
import com.parkmate.authservice.authuser.dto.request.UserRegisterRequestDto;
import com.parkmate.authservice.authuser.dto.response.SocialLoginResponseDto;
import com.parkmate.authservice.authuser.dto.response.UserLoginResponseDto;
import com.parkmate.authservice.authuser.vo.request.*;
import com.parkmate.authservice.authuser.vo.response.EmailDuplicateResponseVo;
import com.parkmate.authservice.authuser.vo.response.SocialLoginResponseVo;
import com.parkmate.authservice.authuser.vo.response.UserLoginResponseVo;
import com.parkmate.authservice.authuser.vo.response.VerifyEmailCodeResponseVo;
import com.parkmate.authservice.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class AuthUserController {

    private final AuthService authService;

    @Operation(
            summary = "일반 로그인",
            description = "이메일과 비밀번호로 일반 사용자가 로그인합니다.",
            tags = {"AUTH-USER-SERVICE"}
    )
    @PostMapping("/login")
    public ApiResponse<UserLoginResponseVo> login(@RequestBody UserLoginRequestVo userLoginRequestVo) {

        UserLoginRequestDto userLoginRequestDto = UserLoginRequestDto.from(userLoginRequestVo);
        UserLoginResponseDto userLoginResponseDto = authService.login(userLoginRequestDto);

        return ApiResponse.of(
                HttpStatus.OK,
                "로그인에 성공했습니다.",
                UserLoginResponseVo.from(userLoginResponseDto)
        );
    }

    @Operation(
            summary = "로그아웃",
            description = "Refresh Token을 삭제하여 일반 사용자가 로그아웃합니다.",
            tags = {"AUTH-USER-SERVICE"}
    )
    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader("X-User-UUID") String userUuid) {

        authService.logout(userUuid);
        return ApiResponse.of(
                HttpStatus.RESET_CONTENT,
                "로그아웃 되었습니다."
        );
    }

    @Operation(
            summary = "회원가입",
            description = "이메일 인증이 완료된 사용자가 회원가입을 진행합니다.",
            tags = {"AUTH-USER-SERVICE"}
    )
    @PostMapping("/register")
    public ApiResponse<String> register(@Valid @RequestBody UserRegisterRequestVo userRegisterRequestVo) {

        UserRegisterRequestDto userRegisterRequestDto = UserRegisterRequestDto.from(userRegisterRequestVo);
        authService.register(userRegisterRequestDto, userRegisterRequestVo);

        return ApiResponse.of(
                HttpStatus.CREATED,
                "회원가입이 완료되었습니다."
        );
    }

    @Operation(
            summary = "이메일 중복 확인",
            description = "사용자의 이메일이 중복되었는지 확인합니다. (boolean 으로 반환)",
            tags = {"AUTH-USER-SERVICE"}
    )
    @PostMapping("/checkEmail")
    public ApiResponse<EmailDuplicateResponseVo> checkEmailDuplicate(@RequestBody EmailDuplicateCheckRequestVo emailDuplicateCheckRequestVo) {

        boolean isDuplicate = authService.isEmailDuplicate(emailDuplicateCheckRequestVo.getEmail());

        return ApiResponse.of(
                HttpStatus.OK,
                "이메일 중복 여부 확인 성공",
                EmailDuplicateResponseVo.of(isDuplicate)
        );
    }

    @Operation(
            summary = "이메일 인증코드 발송",
            description = "사용자 이메일로 인증코드를 발송합니다.",
            tags = {"AUTH-USER-SERVICE"}
    )
    @PostMapping("/sendVerificationCode")
    public ApiResponse<String> sendVerificationCode(@RequestParam String email) {

        authService.sendVerificationCode(email);
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
            tags = {"AUTH-USER-SERVICE"}
    )
    @PostMapping("/verifyCode")
    public ApiResponse<VerifyEmailCodeResponseVo> verifyEmailCode(
            @Valid @RequestBody VerifyEmailCodeRequestVo verifyEmailCodeRequestVo
    ) {
        boolean isValid = authService.verifyEmailCode(
                verifyEmailCodeRequestVo.getEmail(),
                verifyEmailCodeRequestVo.getVerificationCode()
        );

        return ApiResponse.of(
                HttpStatus.OK,
                "이메일 인증번호 검증 성공",
                VerifyEmailCodeResponseVo.of(isValid)
        );
    }

    @Operation(
            summary = "소셜 로그인 및 회원가입",
            description = "소셜 로그인(카카오 등) 또는 소셜 회원가입을 진행합니다.\n\n" +
                    "- X-Social-Access-Token 헤더에 소셜 플랫폼에서 받은 AccessToken을 전달하세요.\n" +
                    "- 현재는 카카오만 지원합니다.",
            tags = {"AUTH-USER-SERVICE"}
    )
    @PostMapping("/socialRegister")
    public ApiResponse<SocialLoginResponseVo> registerSocialUser(

            @RequestHeader("X-Social-Access-Token") String socialAccessToken,
            @Valid @RequestBody SocialRegisterRequestVo socialRegisterRequestVo
    ) {
        SocialLoginResponseDto responseDto = authService.registerSocialUser(socialAccessToken, socialRegisterRequestVo);

        return ApiResponse.of(
                HttpStatus.OK,
                "소셜 로그인 및 회원가입에 성공했습니다.",
                SocialLoginResponseVo.from(responseDto)
        );
    }

    @Operation(
            summary = "UserUuid로 이메일 조회",
            description = "X-User-UUID 헤더를 기반으로 해당 사용자의 이메일을 조회합니다.",
            tags = {"AUTH-USER-SERVICE"}
    )
    @GetMapping("/email")
    public ApiResponse<String> getEmailByUserUuid(@RequestHeader("X-User-UUID") String userUuid) {

        String email = authService.getEmailByUserUuid(userUuid);
        return ApiResponse.of(
                HttpStatus.OK,
                "이메일 조회 성공",
                email
        );
    }
}