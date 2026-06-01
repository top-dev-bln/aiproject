package com.demo.config.thread;

import com.demo.constants.CorrelationConstants;
import com.demo.context.CommonContextHolder;
import com.demo.util.CorrelationUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Slf4j
public class CommonContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        CommonContext commonContext = CommonContextHolder.getContext();
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        SecurityContext securityContext = SecurityContextHolder.getContext();

        log.debug("Capturing context for async operation - correlationId: {}", 
                 commonContext != null ? commonContext.getCorrelationId() : "none");

        return () -> {
            try {
                if (commonContext != null) {
                    CommonContextHolder.setContext(commonContext, true);
                } else {
                    CommonContext newContext = CommonContextHolder.createFromSecurityContext();
                    CommonContextHolder.setContext(newContext, true);
                }

                if (mdcContext != null) {
                    MDC.setContextMap(mdcContext);
                } else {
                    String correlationId = CorrelationUtils.currentCorrelationId();
                    if (correlationId != null) {
                        MDC.put(CorrelationConstants.CONTEXT_CORRELATION_ID.getValue(), correlationId);
                    }
                }

                SecurityContextHolder.setContext(securityContext);

                log.debug("Context restored for async operation - correlationId: {}", 
                         CorrelationUtils.currentCorrelationId());

                runnable.run();

            } finally {
                CommonContextHolder.resetContext();
                MDC.clear();
                SecurityContextHolder.clearContext();

                log.debug("Context cleaned up after async operation");
            }
        };
    }
}
