package com.allen.spring.cloud.nacos.eureka;

import com.alibaba.nacos.api.PropertyKeyConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Properties;

@ConfigurationProperties("spring.cloud.nacos.discovery")
public class NacosDiscoveryProperties extends Properties {

    @Autowired
    private Environment environment;

    @PostConstruct
    public void init() {
        String serverAddr = getProperty(PropertyKeyConst.SERVER_ADDR);
        if (StringUtils.isEmpty(serverAddr)) {
            setProperty(PropertyKeyConst.SERVER_ADDR, environment
                    .resolvePlaceholders("${spring.cloud.nacos.discovery.server-addr:}"));
        }
    }
}
