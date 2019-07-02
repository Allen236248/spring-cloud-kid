package com.allen.spring.cloud.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class AuthenticationFilter extends ZuulFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        LOGGER.info("AuthenticationFilter：request method = " + request.getMethod() + ", and url = " + request.getRequestURL().toString());

        String token = request.getParameter("token");
        if(StringUtils.hasText(token)) {
            context.setSendZuulResponse(true);//对请求进行路由
            context.setResponseStatusCode(200);
            context.set("code", 0);
            return null;
        }

        //如果请求没有token，则为无效请求
        context.setSendZuulResponse(false);
        context.setResponseStatusCode(400);
        context.setResponseBody("The token is empty");
        context.set("code", -1);
        return null;
    }
}
