package com.allen.spring.cloud.consumer.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
@FeignClient(name = "spring-cloud-kid-002")
public interface HelloFeign {

    @RequestMapping("/hello")
    String hello(@RequestParam("name") String name);

    @RequestMapping("/service_info")
    List<String> serviceInfo();
}
