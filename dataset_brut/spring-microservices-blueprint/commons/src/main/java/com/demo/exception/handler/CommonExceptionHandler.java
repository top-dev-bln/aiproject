package com.demo.exception.handler;

import com.demo.exception.ApplicationException;
import com.demo.exception.FeignResponseException;
import com.demo.exception.model.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@RestControllerAdvice
@Slf4j
public class CommonExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception ex) {
        log.error(ExceptionUtils.getMessage(ex));
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiError errorModel = new ApiError(httpStatus.value(), "An error occurred");
        return new ResponseEntity<>(errorModel, header(), httpStatus);
    }

    @ExceptionHandler({ApplicationException.class})
    public ResponseEntity<ApiError> handleBaseException(ApplicationException ex, WebRequest request) {
        log.error(ExceptionUtils.getMessage(ex));
        ApiError errorModel = new ApiError(ex.getHttpStatus().value(), ex.getMessages());
        return new ResponseEntity<>(errorModel, ex.getHttpStatus());
    }

    @ExceptionHandler({FeignResponseException.class})
    public ResponseEntity<Object> handleFeignException(Exception ex, WebRequest request) {
        log.error(ExceptionUtils.getMessage(ex));
        FeignResponseException customEx = (FeignResponseException) ex;
        ApiError errorModel = customEx.getErrorModel();
        HttpStatus httpStatus = customEx.getHttpStatus();
        return new ResponseEntity<>(errorModel, header(), httpStatus);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<Object> handleAccessDenied(Exception ex, WebRequest request) {
        log.error(ExceptionUtils.getMessage(ex));
        HttpStatus httpStatus = HttpStatus.FORBIDDEN;
        ApiError errorModel = new ApiError(httpStatus.value(), "Forbidden");
        return new ResponseEntity<>(errorModel, header(), httpStatus);
    }

    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<Object> handleAuthentication(Exception ex, WebRequest request) {
        log.error(ExceptionUtils.getMessage(ex));
        HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
        ApiError errorModel = new ApiError(httpStatus.value(), "Unauthorized");
        return new ResponseEntity<>(errorModel, header(), httpStatus);
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class})
    public ResponseEntity<Object> handleMaxUploadSizeException(Exception ex, WebRequest request) {
        log.error(ExceptionUtils.getMessage(ex));
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ApiError errorModel = new ApiError(
                httpStatus.value(),
                "The max size is: {}" + ((MaxUploadSizeExceededException) ex).getMaxUploadSize()
        );
        return new ResponseEntity<>(errorModel, header(), httpStatus);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ResponseEntity<Object> handleHttpMessageNotReadableException(Exception ex, WebRequest request) {
        log.error(ExceptionUtils.getMessage(ex));
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ApiError errorModel = new ApiError(
                httpStatus.value(),
                ex.getMessage()
        );
        return new ResponseEntity<>(errorModel, header(), httpStatus);
    }

    private HttpHeaders header(){
        HttpHeaders customHeader = new HttpHeaders();
        customHeader.setContentType(MediaType.APPLICATION_JSON);
        return customHeader;
    }

}
