package com.allen.spring.cloud.consumer.service.impl;

import com.allen.spring.cloud.producer.feign.HelloFeign;
import com.allen.spring.cloud.consumer.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HelloServiceImpl implements HelloService {

    @Autowired
    private HelloFeign helloFeign;

    @Override
    public String hello(String name) {
        return helloFeign.hello(name);
    }

    @Override
    public List<String> serviceInfo() {
        return helloFeign.serviceInfo();
    }
}
