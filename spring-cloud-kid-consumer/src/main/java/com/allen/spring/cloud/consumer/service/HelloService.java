package com.allen.spring.cloud.consumer.service;

import java.util.List;

public interface HelloService {

    String hello(String name);

    List<String> serviceInfo();

}
