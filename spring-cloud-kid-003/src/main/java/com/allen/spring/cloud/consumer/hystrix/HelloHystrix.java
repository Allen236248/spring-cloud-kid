package com.allen.spring.cloud.consumer.hystrix;

import com.allen.spring.cloud.consumer.feign.HelloFeign;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Component
public class HelloHystrix implements HelloFeign {

    @Override
    public String hello(@RequestParam("name") String name) {
        return "Hello " + name + ", this is a hystrix message.";
    }

    @Override
    public List<String> serviceInfo() {
        return new ArrayList<>();
    }
}
