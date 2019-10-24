package com.allen.cloud.producer.hystrix;

import com.allen.cloud.producer.feign.HelloFeign;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HelloHystrix implements HelloFeign {

    @Override
    public String hello(String name) {
        return "Hello " + name + ", this is a hystrix message.";
    }

    @Override
    public List<String> serviceInfo() {
        return new ArrayList<>();
    }

    @Override
    public String showOwner(String owner) {
        return "This is a hystrix message.";
    }
}
