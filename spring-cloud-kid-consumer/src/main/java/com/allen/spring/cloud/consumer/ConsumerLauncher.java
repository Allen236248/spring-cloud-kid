package com.allen.spring.cloud.consumer;

import com.allen.spring.cloud.consumer.configuration.HystrixConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@EnableHystrixDashboard
/**
 * 启用服务注册与发现
 * Spring Cloud中服务注册与发现有许多种实现（eureka、consul、zookeeper等等），
 * @EnableDiscoveryClient基于spring-cloud-commons,
 * @EnableEurekaClient基于spring-cloud-netflix。
 * 如果选用的注册中心是eureka，那么就推荐@EnableEurekaClient
 * 如果是其他的注册中心，那么推荐使用@EnableDiscoveryClient
 */
@EnableEurekaClient
//启用feign进行远程调用。注意如果定义的Feign接口和启动类不在同一个包路径下，需要指定basePackages，表示Feign接口所在的包路径
/**
 * Feign + Hystrix 实现服务容错保护 EnableCircuitBreaker
 * 断路器，不需要在主类使用@EnableCircuitBreaker，Feign已自动开启该功能
 * 不需要在FeignClient的interface上使用 @HystrixCommand，已隐含 EnableFeignClients 负载均衡客户端
 * EnableDiscoveryClient 服务发现注册
 */
@EnableFeignClients
@SpringBootApplication
@RibbonClient(name = "spring-cloud-kid-producer", configuration = HystrixConfiguration.class)
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
