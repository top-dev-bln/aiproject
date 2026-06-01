package com.demo.logging;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Slf4j
@ControllerAdvice
public class CommonRequestBodyLogger extends RequestBodyAdviceAdapter {

    @Value("${management.endpoints.web.base-path:/actuator}")
    private String actuatorBasePath;

    @Autowired
    HttpServletRequest httpServletRequest;

    @Override
    public boolean supports(MethodParameter methodParameter, Type type,
                            Class<? extends HttpMessageConverter<?>> aClass) {
        String uri = httpServletRequest.getRequestURI();
        return !uri.startsWith(actuatorBasePath);
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage,
                                MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        log.info("Request body: URI={}, QueryString={}, Payload={}", 
                httpServletRequest.getRequestURI(),
                httpServletRequest.getQueryString(),
                body);
        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
    }

    @Override
    public Object handleEmptyBody(@Nullable Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                  Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        if(httpServletRequest.getQueryString() != null) {
            log.info("Request empty body: URI={}, QueryString={}", 
                    httpServletRequest.getRequestURI(),
                    httpServletRequest.getQueryString());
        }
        return body;
    }
}
