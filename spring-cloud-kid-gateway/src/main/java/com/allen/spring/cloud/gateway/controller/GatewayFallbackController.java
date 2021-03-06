package com.allen.spring.cloud.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GatewayFallbackController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayFallbackController.class);

    @RequestMapping("/gateway_fallback")
    public String gatewayFallback() {
        return "Error";
    }

}
