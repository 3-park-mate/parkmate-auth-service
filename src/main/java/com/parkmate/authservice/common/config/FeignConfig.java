package com.parkmate.authservice.common.config;

import feign.codec.Decoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class FeignConfig {

    @Bean
    public Decoder feignDecoder() {

        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new MappingJackson2XmlHttpMessageConverter());
        return new SpringDecoder(() -> new HttpMessageConverters(converters));
    }
}