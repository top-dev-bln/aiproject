package com.demo.constants;

import lombok.Getter;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Getter
public enum CorrelationConstants {
    CONTEXT_CORRELATION_ID("correlation_id"),
    CONTEXT_REQUEST_URL("request_url"),
    CONTEXT_REQUEST_METHOD("request_method"),
    CONTEXT_REQUEST_TYPE("request_type"),
    CONTEXT_RESPONSE_TIME("response_time"),
    CONTEXT_RESPONSE_STATUS("http_status");

    private final String value;

    CorrelationConstants(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
