package com.parkmate.authservice.authhost.infrastructure.client;

import com.parkmate.authservice.authhost.dto.response.BiznoValidationResponseDto;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "biznoClient",
        url = "https://bizno.net/api"
)
public interface BiznoFeignClient {

    @GetMapping(value = "/fapi", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Headers("Content-Type: application/x-www-form-urlencoded")
    BiznoValidationResponseDto validateBizNumber(
            @RequestParam("key") String key,
            @RequestParam("gb") String gb,
            @RequestParam("q") String query,
            @RequestParam("type") String type
    );
}