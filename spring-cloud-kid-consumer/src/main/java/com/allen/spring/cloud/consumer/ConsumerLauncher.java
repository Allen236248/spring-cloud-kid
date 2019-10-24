package com.allen.spring.cloud.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

//@EnableHystrixDashboard
//启用服务注册与发现
@EnableDiscoveryClient
//启用feign进行远程调用。注意如果定义的Feign接口和启动类不在同一个包路径下，需要指定basePackages，表示Feign接口所在的包路径
@EnableFeignClients("com.allen.spring.cloud.producer")
@ComponentScan({"com.allen.spring.cloud.consumer", "com.allen.spring.cloud.producer"})
@SpringBootApplication
public class ConsumerLauncher {

    public static void main(String...args) {
        SpringApplication.run(ConsumerLauncher.class, args);
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
