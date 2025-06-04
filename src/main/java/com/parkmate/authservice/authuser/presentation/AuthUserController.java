package com.parkmate.authservice.authuser.presentation;

import com.parkmate.authservice.authuser.application.AuthService;
import com.parkmate.authservice.authuser.dto.request.SocialLoginRequestDto;
import com.parkmate.authservice.authuser.dto.request.SocialRegisterRequestDto;
import com.parkmate.authservice.authuser.dto.request.UserLoginRequestDto;
import com.parkmate.authservice.authuser.dto.request.UserRegisterRequestDto;
import com.parkmate.authservice.authuser.dto.response.SocialLoginResponseDto;
import com.parkmate.authservice.authuser.dto.response.UserLoginResponseDto;
import com.parkmate.authservice.authuser.vo.request.*;
import com.parkmate.authservice.authuser.vo.response.SocialLoginResponseVo;
import com.parkmate.authservice.authuser.vo.response.UserLoginResponseVo;
import com.parkmate.authservice.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthUserController {

    private final AuthService authService;

    @PostMapping("/login/user")
    public ApiResponse<UserLoginResponseVo> login(@RequestBody UserLoginRequestVo userLoginRequestVo) {

        UserLoginRequestDto userLoginRequestDto = UserLoginRequestDto.from(userLoginRequestVo);
        UserLoginResponseDto userLoginResponseDto = authService.login(userLoginRequestDto);

        return ApiResponse.of(
                HttpStatus.OK,
                "요청에 성공했습니다.",
                UserLoginResponseVo.from(userLoginResponseDto)
        );
    }

    @PostMapping("/logout/user")
    public ApiResponse<String> logout(@RequestParam("useruuid") String userUuid) {

        authService.logout(userUuid);
        return ApiResponse.of(
                HttpStatus.RESET_CONTENT,
                "로그아웃 되었습니다."
        );
    }

    @PostMapping("/register/user")
    public ApiResponse<String> register(@Valid @RequestBody UserRegisterRequestVo userRegisterRequestVo) {

        UserRegisterRequestDto userRegisterRequestDto = UserRegisterRequestDto.from(userRegisterRequestVo);
        authService.register(userRegisterRequestDto, userRegisterRequestVo);

        return ApiResponse.of(
                HttpStatus.CREATED,
                "회원가입이 완료되었습니다."
        );
    }

    @GetMapping("/checkEmail")
    public ApiResponse<String> checkEmailDuplicate(@RequestParam String email) {

        authService.checkEmailDuplicate(email);
        return ApiResponse.of(
                HttpStatus.OK,
                "이메일 중복 검사 완료되었습니다."
        );
    }

    @PostMapping("/sendVerificationCode")
    public ApiResponse<String> sendVerificationCode(@RequestParam String email) {

        authService.sendVerificationCode(email);
        return ApiResponse.of(
                HttpStatus.OK,
                "이메일 인증번호 전송이 완료되었습니다."
        );
    }

    @PostMapping("/verifyCode")
    public ApiResponse<String> verifyEmailCode(@Valid @RequestBody VerifyEmailCodeRequestVo verifyEmailCodeRequestVo) {

        authService.verifyEmailCode(
                verifyEmailCodeRequestVo.getEmail(),
                verifyEmailCodeRequestVo.getVerificationCode()
        );
        return ApiResponse.of(
                HttpStatus.OK,
                "이메일 인증번호 검증이 완료되었습니다."
        );
    }

    @PostMapping("/socialLogin")
    public ApiResponse<SocialLoginResponseVo> loginSocialUser(@RequestBody SocialLoginRequestVo socialLoginRequestVo) {

        SocialLoginRequestDto socialLoginRequestDto = SocialLoginRequestDto.from(socialLoginRequestVo);
        SocialLoginResponseDto responseDto = authService.loginSocialUser(socialLoginRequestDto);

        return ApiResponse.of(
                HttpStatus.OK,
                "요청에 성공했습니다.",
                SocialLoginResponseVo.from(responseDto)
        );
    }

    @PostMapping("/socialRegister")
    public ApiResponse<String> registerSocialUser(
            @RequestHeader("Authorization") String bearerToken,
            @Valid @RequestBody SocialRegisterRequestVo socialRegisterRequestVo
    ) {
        String accessToken = bearerToken.replace("Bearer ", "");
        authService.registerSocialUser(accessToken, socialRegisterRequestVo);

        return ApiResponse.of(
                HttpStatus.CREATED,
                "회원가입이 완료되었습니다."
        );
    }
}