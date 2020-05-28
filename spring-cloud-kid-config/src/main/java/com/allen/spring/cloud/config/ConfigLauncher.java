package com.allen.spring.cloud.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class ConfigLauncher {

    public static void main(String...args) {
        SpringApplication.run(ConfigLauncher.class, args);
    }

}
