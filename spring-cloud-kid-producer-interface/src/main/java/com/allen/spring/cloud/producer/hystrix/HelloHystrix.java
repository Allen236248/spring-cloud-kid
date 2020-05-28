package com.allen.spring.cloud.producer.hystrix;

import com.allen.spring.cloud.producer.feign.HelloFeign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class HelloHystrix implements HelloFeign {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloHystrix.class);

    @Override
    public String hello(String name) {
        LOGGER.info(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
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
