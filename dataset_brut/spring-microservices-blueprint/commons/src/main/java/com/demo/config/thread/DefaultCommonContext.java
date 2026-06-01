package com.demo.config.thread;

import lombok.Data;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Data
public class DefaultCommonContext implements CommonContext {
    private String correlationId;
    private String userId;
    private String username;

    public DefaultCommonContext() {
    }

    public DefaultCommonContext(String correlationId, String userId, String username) {
        this.correlationId = correlationId;
        this.userId = userId;
        this.username = username;
    }
}
