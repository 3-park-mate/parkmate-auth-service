package com.parkmate.authservice.authuser.infrastructure.client;

import com.parkmate.authservice.authuser.dto.request.feign.UserRegisterRequestForUserServiceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service")
public interface UserFeignClient {

    @PostMapping("/internal/users/register")
    void registerUser(@RequestBody UserRegisterRequestForUserServiceDto userRegisterRequestForUserServiceDto);
}
