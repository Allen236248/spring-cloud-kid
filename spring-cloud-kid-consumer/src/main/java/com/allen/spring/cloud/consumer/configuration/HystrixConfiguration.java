package com.allen.spring.cloud.consumer.configuration;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.RandomRule;
import com.netflix.loadbalancer.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HystrixConfiguration {

    @Bean(name = "hystrixRegistrationBean")
    public ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new HystrixMetricsStreamServlet(), "/hystrix.stream");
        registration.setName("hystrixServlet");
        registration.setLoadOnStartup(1);
        return registration;
    }

    @Bean
    public CustomRandomLoadBalancerRule customRandomLoadBalancerRule() {
        return new CustomRandomLoadBalancerRule();
    }

    public static class CustomRandomLoadBalancerRule extends RandomRule {

        private static final Logger LOGGER = LoggerFactory.getLogger(CustomRandomLoadBalancerRule.class);

        @Override
        public Server choose(ILoadBalancer lb, Object key) {
            LOGGER.info("CustomRandomLoadBalancerRule#choose(ILoadBalancer, Object)");
            return super.choose(lb, key);
        }

        @Override
        public Server choose(Object key) {
            LOGGER.info("CustomRandomLoadBalancerRule#choose(Object)");
            return super.choose(key);
        }
    }
}
