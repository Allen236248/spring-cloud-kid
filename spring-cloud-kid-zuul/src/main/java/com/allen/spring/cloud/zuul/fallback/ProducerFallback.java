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
public class ProducerFallback implements FallbackProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerFallback.class);

    @Override
    public String getRoute() {
        return "spring-cloud-kid-producer";
    }

    @Override
    public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
        if(cause != null) {
            String causeMsg = cause.getMessage();
            LOGGER.error("降级收到来自" + route + "的服务异常：" + cause);
            return new ThrowableClientHttpResponse(causeMsg);
        }
        return null;
    }

    private static class ThrowableClientHttpResponse implements ClientHttpResponse {

        private String causeMsg;

        public ThrowableClientHttpResponse(String causeMsg) {
            this.causeMsg = causeMsg;
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
            return new ByteArrayInputStream(causeMsg.getBytes());
        }

        @Override
        public HttpHeaders getHeaders() {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            return headers;
        }
    }
}
