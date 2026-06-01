package com.demo.exception;

import com.demo.exception.model.ApiError;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Getter
public class FeignResponseException extends ApplicationException {

    private final HttpStatus status;

    private final transient ApiError errorModel;

    public FeignResponseException(ApiError errorModel) {
        this.status = HttpStatus.valueOf(errorModel.getStatusCode());
        this.errorModel = errorModel;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.status;
    }
}
