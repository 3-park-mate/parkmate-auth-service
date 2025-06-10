package com.parkmate.authservice.authuser.application;

import com.parkmate.authservice.authuser.dto.request.UserLoginRequestDto;
import com.parkmate.authservice.authuser.dto.request.UserRegisterRequestDto;
import com.parkmate.authservice.authuser.dto.response.SocialLoginResponseDto;
import com.parkmate.authservice.authuser.dto.response.UserLoginResponseDto;
import com.parkmate.authservice.authuser.vo.request.SocialRegisterRequestVo;
import com.parkmate.authservice.authuser.vo.request.UserRegisterRequestVo;

public interface AuthService {

    UserLoginResponseDto login(UserLoginRequestDto userLoginRequestDto);

    void logout(String userUuid);

    void register(UserRegisterRequestDto userRegisterRequestDto, UserRegisterRequestVo userRegisterRequestVo);

    boolean isEmailDuplicate(String email);

    void sendVerificationCode(String email);

    boolean verifyEmailCode(String email, String code);

    SocialLoginResponseDto registerSocialUser(String socialAccessToken, SocialRegisterRequestVo socialRegisterRequestVo);
}
