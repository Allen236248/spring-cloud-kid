package com.allen.spring.cloud.consumer.service.impl;

import com.allen.spring.cloud.consumer.service.feign.HelloFeign;
import com.allen.spring.cloud.consumer.service.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class HelloServiceImpl implements HelloService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Autowired
    private HelloFeign helloFeign;

    @Override
    public String hello(String name) {
        LOGGER.info(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        return helloFeign.hello(name);
    }

    @Override
    public List<String> serviceInfo() {
        return helloFeign.serviceInfo();
    }
}
