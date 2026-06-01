package com.demo.context;

import com.demo.config.thread.CommonContext;
import com.demo.config.thread.DefaultCommonContext;
import com.demo.constants.CorrelationConstants;
import com.demo.util.CorrelationUtils;
import org.slf4j.MDC;
import org.springframework.core.NamedInheritableThreadLocal;
import org.springframework.core.NamedThreadLocal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.annotation.Nullable;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

public abstract class CommonContextHolder {
    private static final ThreadLocal<CommonContext> COMMON_CONTEXT_HOLDER =
            new NamedThreadLocal<>("Common context");
    private static final ThreadLocal<CommonContext> INHERITABLE_COMMON_CONTEXT_HOLDER =
            new NamedInheritableThreadLocal<>("Common context");

    private CommonContextHolder() {
        // Utility class - prevent instantiation
    }

    public static void resetContext() {
        COMMON_CONTEXT_HOLDER.remove();
        INHERITABLE_COMMON_CONTEXT_HOLDER.remove();
    }

    public static void setContext(@Nullable CommonContext context) {
        setContext(context, false);
    }

    public static void setContext(@Nullable CommonContext context, boolean inheritable) {
        if (context == null) {
            resetContext();
        } else {
            if (inheritable) {
                INHERITABLE_COMMON_CONTEXT_HOLDER.set(context);
                COMMON_CONTEXT_HOLDER.remove();
            } else {
                COMMON_CONTEXT_HOLDER.set(context);
                INHERITABLE_COMMON_CONTEXT_HOLDER.remove();
            }
        }
    }

    @Nullable
    public static CommonContext getContext() {
        CommonContext context = COMMON_CONTEXT_HOLDER.get();
        if (context == null) {
            context = INHERITABLE_COMMON_CONTEXT_HOLDER.get();
        }
        return context;
    }

    public static String getCorrelationId() {
        CommonContext context = getContext();
        if (context != null && context.getCorrelationId() != null) {
            return context.getCorrelationId();
        }
        return MDC.get(CorrelationConstants.CONTEXT_CORRELATION_ID.getValue());
    }

    @Nullable
    public static String getUserId() {
        CommonContext context = getContext();
        if (context != null) {
            return context.getUserId();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                return authentication.getName();
            }
        }
        return null;
    }

    @Nullable
    public static String getUsername() {
        CommonContext context = getContext();
        if (context != null) {
            return context.getUsername();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return null;
    }

    public static CommonContext createFromSecurityContext() {
        String correlationId = CorrelationUtils.currentCorrelationId();
        String username = getUsername();
        String userId = getUserId();

        return new DefaultCommonContext(correlationId, userId, username);
    }
}
