package com.allen.spring.cloud.producer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HelloController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloController.class);

    @Autowired
    private DiscoveryClient discoveryClient;

    @RequestMapping("/hello")
    public String hello(@RequestParam(value = "name", required = false) String name) {
        LOGGER.info("request parameter is : " + name);
        return "Hello " + name + ", this is the first message.";
    }

    @RequestMapping("/service_info")
    public List<String> serviceInfo() {
        return discoveryClient.getServices();
    }

    @RequestMapping("/show_owner")
    public String showOwner(@RequestParam(value = "owner", required = false) String owner) {
        LOGGER.info("The owner is : " + owner);
        return "The owner is : " + owner;
    }

}
