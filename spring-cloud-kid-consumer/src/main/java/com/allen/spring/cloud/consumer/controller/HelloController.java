package com.allen.spring.cloud.consumer.controller;

import com.allen.spring.cloud.consumer.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
public class HelloController {

    @Autowired
    private HelloService helloService;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/hello_service_info")
    public String hello(@RequestParam("name") String name) {
        String res = helloService.hello(name);
        List<String> list = helloService.serviceInfo();
        for(String s : list) {
            res += s;
        }
        return res;
    }

    /**
     * Ribbon可提供负载均衡；Feign也可以提供负载均衡
     *
     * @return
     */
    @RequestMapping("/show_owner")
    public String ribbonLoadBalance() {
        String url = "http://spring-cloud-kid-producer/show_owner?owner=123";
        return restTemplate.getForObject(url, String.class);
    }

}
