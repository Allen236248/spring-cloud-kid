package com.allen.spring.cloud.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@EnableEurekaClient 为什么Spring Cloud Gateway可以不用此注解
@SpringBootApplication
public class GatewayLauncher {

    public static void main(String...args) {
        SpringApplication.run(GatewayLauncher.class, args);
    }

}
