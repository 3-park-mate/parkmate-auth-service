package com.parkmate.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@EnableFeignClients(basePackages = "com.parkmate.authservice")
@EnableDiscoveryClient
@SpringBootApplication
public class AuthserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthserviceApplication.class, args);
	}

}
