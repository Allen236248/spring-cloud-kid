package com.allen.spring.cloud.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class EurekaLauncher {

    public static void main(String...args) {
        SpringApplication.run(EurekaLauncher.class, args);
    }
}
