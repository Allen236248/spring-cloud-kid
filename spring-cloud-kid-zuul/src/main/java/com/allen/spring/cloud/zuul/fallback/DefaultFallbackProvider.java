package com.allen.spring.cloud.zuul.fallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class DefaultFallbackProvider implements FallbackProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFallbackProvider.class);

    @Override
    public String getRoute() {
        //指明熔断拦截哪个服务：指定某个route，以告诉Zuul负责该route定义下的服务访问出问题后的熔断处理
        return "*";
    }

    /**
     * 定制返回内容
     *
     * @param route
     * @param cause
     * @return
     */
    @Override
    public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
        if(cause != null) {
            LOGGER.error("降级收到来自" + route + "的服务异常：" + cause);
            return new ThrowableClientHttpResponse(cause);
        }
        return null;
    }

    private static class ThrowableClientHttpResponse implements ClientHttpResponse {

        private Throwable cause;

        public ThrowableClientHttpResponse(Throwable cause) {
            this.cause = cause;
        }

        @Override
        public HttpStatus getStatusCode() throws IOException {
            return HttpStatus.OK;
        }

        @Override
        public int getRawStatusCode() throws IOException {
            return 200;
        }

        @Override
        public String getStatusText() throws IOException {
            return "OK";
        }

        @Override
        public void close() {

        }

        @Override
        public InputStream getBody() throws IOException {
            return new ByteArrayInputStream(("服务不可用：" + cause.getMessage()).getBytes());
        }

        @Override
        public HttpHeaders getHeaders() {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            return headers;
        }
    }
}
