package com.allen.spring.cloud.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

//启动服务注册与发现
@EnableEurekaClient
@EnableDiscoveryClient
@SpringBootApplication
public class ProducerLauncher {

    public static void main(String...args) {
        SpringApplication.run(ProducerLauncher.class, args);
    }
}
