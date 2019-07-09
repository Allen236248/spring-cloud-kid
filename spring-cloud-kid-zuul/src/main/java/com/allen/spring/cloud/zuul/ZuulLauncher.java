package com.allen.spring.cloud.zuul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@EnableZuulProxy
// spring cloud中discovery service有许多种实现（eureka、consul、zookeeper等等），@EnableDiscoveryClient基于spring-cloud-commons, @EnableEurekaClient基于spring-cloud-netflix。
// 如果选用的注册中心是eureka，那么就推荐@EnableEurekaClient
// 如果是其他的注册中心，那么推荐使用@EnableDiscoveryClient
@EnableEurekaClient
@SpringBootApplication
public class ZuulLauncher {

    public static void main(String...args) {
        SpringApplication.run(ZuulLauncher.class, args);
    }
}
