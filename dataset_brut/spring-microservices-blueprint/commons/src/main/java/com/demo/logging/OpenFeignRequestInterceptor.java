package com.demo.logging;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import com.demo.constants.CorrelationConstants;
import com.demo.util.CorrelationUtils;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

public class OpenFeignRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        String correlationId = CorrelationUtils.currentCorrelationId();
        requestTemplate.header(CorrelationConstants.CONTEXT_CORRELATION_ID.getValue(), correlationId);
    }
}
