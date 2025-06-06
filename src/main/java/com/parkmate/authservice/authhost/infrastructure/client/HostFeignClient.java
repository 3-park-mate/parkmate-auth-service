package com.parkmate.authservice.authhost.infrastructure.client;

import com.parkmate.authservice.authhost.dto.request.feign.HostRegisterRequestForHostServiceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "host-service")
public interface HostFeignClient {

    @PostMapping("/internal/hosts/register")
    void registerHost(@RequestBody HostRegisterRequestForHostServiceDto hostRegisterRequestForHostServiceDto);
}