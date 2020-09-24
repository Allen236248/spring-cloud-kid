package com.allen.spring.cloud.consumer.service.feign;

import com.allen.spring.cloud.consumer.service.hystrix.HelloHystrix;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @FeignClient 需要指向的是服务提供者
 */
//name:远程服务名，即：spring.application.name配置的名称
@FeignClient(name = "spring-cloud-kid-producer", fallback = HelloHystrix.class)
public interface HelloFeign {

    //方法名和参数必须与远程服务中contoller中的保持一致。
    @RequestMapping("/hello")
    String hello(@RequestParam(value = "name", required = false) String name);

    @RequestMapping("/service_info")
    List<String> serviceInfo();

    @RequestMapping("/show_owner")
    String showOwner(@RequestParam(value = "owner", required = false) String owner);
}
