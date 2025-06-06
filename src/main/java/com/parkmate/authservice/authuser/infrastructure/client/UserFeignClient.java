package com.parkmate.authservice.authuser.infrastructure.client;

import com.parkmate.authservice.authuser.dto.request.feign.UserRegisterRequestForUserServiceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserFeignClient {

    @GetMapping("/internal/users/name")
    String  findNameByEmail(@RequestParam("email") String email);

    @PostMapping("/internal/users/register")
    void registerUser(@RequestBody UserRegisterRequestForUserServiceDto userRegisterRequestForUserServiceDto);

    @PostMapping("/internal/users/register/social")
    void registerSocialUser(@RequestBody UserRegisterRequestForUserServiceDto userRegisterRequestForUserServiceDto);

}
