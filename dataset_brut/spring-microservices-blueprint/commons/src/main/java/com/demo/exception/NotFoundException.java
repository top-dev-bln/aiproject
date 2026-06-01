package com.demo.exception;

import org.springframework.http.HttpStatus;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

public class NotFoundException extends ApplicationException {
    public NotFoundException() {
        super("Object not found");
    }
    public NotFoundException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
