package com.parkmate.authservice.authuser.application;

import com.parkmate.authservice.authuser.dto.request.SocialLoginRequestDto;
import com.parkmate.authservice.authuser.dto.request.SocialRegisterRequestDto;
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

    void checkEmailDuplicate(String email);

    void sendVerificationCode(String email);

    void verifyEmailCode(String email, String code);

    SocialLoginResponseDto loginSocialUser(SocialLoginRequestDto socialLoginRequestDto);

    void registerSocialUser(String accessToken, SocialRegisterRequestVo vo);
}
