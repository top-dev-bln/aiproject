package com.demo.exception.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ApiError {
    private int statusCode;
    private List<String> messages;

    public ApiError(int statusCode, List<String> messages) {
        this.statusCode = statusCode;
        this.messages = messages;
    }

    public ApiError(int statusCode, String message) {
        this.statusCode = statusCode;
        this.messages = List.of(message);
    }

}
