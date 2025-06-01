package com.parkmate.authservice.authuser.application;

import com.parkmate.authservice.authuser.dto.request.UserLoginRequestDto;
import com.parkmate.authservice.authuser.dto.request.UserRegisterRequestDto;
import com.parkmate.authservice.authuser.dto.response.UserLoginResponseDto;

public interface AuthService {

    UserLoginResponseDto login(UserLoginRequestDto requestDto);

    void logout(String userUuid);

    void register(UserRegisterRequestDto userRegisterRequestDto);
}
