package com.allen.spring.cloud.consumer.feign;

import com.allen.spring.cloud.consumer.hystrix.HelloHystrix;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @FeignClient 需要指向的是服务提供者
 */
@Component
@FeignClient(name = "spring-cloud-kid-producer", fallback = HelloHystrix.class)
public interface HelloFeign {

    @RequestMapping("/hello")
    String hello(@RequestParam("name") String name);

    @RequestMapping("/service_info")
    List<String> serviceInfo();
}
