package com.demo.config.openfeign;

import com.demo.exception.FeignResponseException;
import com.demo.exception.HttpException;
import com.demo.exception.model.ApiError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Slf4j
public class CommonOpenFeignErrorDecoder implements ErrorDecoder {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        ApiError apiError;
        InputStream inputStream;
        String responseBody;
        HttpStatus responseStatus = HttpStatus.valueOf(response.status());

        if (response.body() == null) {
            return new HttpException(responseStatus, List.of(responseStatus.getReasonPhrase()));
        }

        try {
            inputStream = response.body().asInputStream();
            responseBody = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            log.info(response.request().url());
            log.info(responseBody);
        } catch (IOException e) {
            return new HttpException(responseStatus, List.of(responseStatus.getReasonPhrase()));
        }
        try {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            apiError = mapper.readValue(responseBody, ApiError.class);
        } catch (JsonProcessingException e) {
            return new HttpException(responseStatus, List.of(responseBody));
        }

        return new FeignResponseException(apiError);
    }
}
