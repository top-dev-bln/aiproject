package com.demo.exception;

import org.springframework.http.HttpStatus;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

public class FormValidateException extends ApplicationException {

    public FormValidateException() {
        super();
    }

    public FormValidateException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
