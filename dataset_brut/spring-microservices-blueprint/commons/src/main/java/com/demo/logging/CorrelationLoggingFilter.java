package com.demo.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import com.demo.constants.CorrelationConstants;
import com.demo.util.CorrelationUtils;

import java.io.IOException;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Slf4j
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationLoggingFilter implements Filter {

    @Value("${management.endpoints.web.base-path:/actuator}")
    private String actuatorBasePath;

    /**
     * Purpose: Attach correlation and request/response metadata to the logging MDC for
     * end-to-end tracing. Generates or propagates correlation_id, records URL/method,
     * measures latency, logs HTTP status, and cleans up response-specific MDC keys.
     * Actuator endpoints are skipped to reduce noise.
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Skip actuator endpoints
        if (actuatorBasePath != null && !actuatorBasePath.isBlank() && request.getRequestURI().startsWith(actuatorBasePath)) {
            chain.doFilter(req, res);
            return;
        }

        // Correlation and request MDC context
        String correlationId = request.getHeader(CorrelationConstants.CONTEXT_CORRELATION_ID.getValue());
        if (!StringUtils.hasText(correlationId)) {
            correlationId = CorrelationUtils.generateCorrelationId();
        }
        response.setHeader(CorrelationConstants.CONTEXT_CORRELATION_ID.getValue(), correlationId);
        MDC.put(CorrelationConstants.CONTEXT_CORRELATION_ID.getValue(), correlationId);
        MDC.put(CorrelationConstants.CONTEXT_REQUEST_URL.getValue(), request.getRequestURI());
        MDC.put(CorrelationConstants.CONTEXT_REQUEST_METHOD.getValue(), request.getMethod());
        MDC.put(CorrelationConstants.CONTEXT_REQUEST_TYPE.getValue(), "request");

        long startTime = System.currentTimeMillis();
        try {
            chain.doFilter(req, res);
        } finally {
            // Response MDC context, logging, and cleanup
            MDC.put(CorrelationConstants.CONTEXT_REQUEST_TYPE.getValue(), "response");
            MDC.put(CorrelationConstants.CONTEXT_RESPONSE_TIME.getValue(), String.valueOf(System.currentTimeMillis() - startTime));
            MDC.put(CorrelationConstants.CONTEXT_RESPONSE_STATUS.getValue(), String.valueOf(response.getStatus()));
            log.info(HttpStatus.valueOf(response.getStatus()).name());
            MDC.remove(CorrelationConstants.CONTEXT_RESPONSE_TIME.getValue());
            MDC.remove(CorrelationConstants.CONTEXT_RESPONSE_STATUS.getValue());
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // No initialization needed
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}
