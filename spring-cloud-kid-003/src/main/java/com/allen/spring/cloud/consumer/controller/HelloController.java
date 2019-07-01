package com.allen.spring.cloud.consumer.controller;

import com.allen.spring.cloud.consumer.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HelloController {

    @Autowired
    private HelloService helloService;

    @RequestMapping("/hello_service_info")
    public String hello(@RequestParam("name") String name) {
        String res = helloService.hello(name);
        List<String> list = helloService.serviceInfo();
        for(String s : list) {
            res += s;
        }
        return res;
    }

}
