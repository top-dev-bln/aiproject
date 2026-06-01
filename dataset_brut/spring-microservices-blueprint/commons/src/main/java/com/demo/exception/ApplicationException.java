package com.demo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Getter
public abstract class ApplicationException extends RuntimeException {
    public abstract HttpStatus getHttpStatus();

    private final List<String> messages;

    protected ApplicationException() {
        this.messages = null;
    }

    protected ApplicationException(String... messages) {
        this.messages = Arrays.asList(messages);
    }

}
