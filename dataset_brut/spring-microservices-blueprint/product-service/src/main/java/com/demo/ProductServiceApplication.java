package com.demo;

import com.demo.logging.OpenFeignRequestInterceptor;
import com.demo.logging.CommonRequestBodyLogger;
import com.demo.logging.CommonResponseBodyLogger;
import com.demo.exception.handler.CommonExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import com.demo.util.StringToDateConverter;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@SpringBootApplication
@EnableFeignClients(defaultConfiguration = {OpenFeignRequestInterceptor.class})
@Import({StringToDateConverter.class, CommonRequestBodyLogger.class, CommonResponseBodyLogger.class, CommonExceptionHandler.class})
@ImportAutoConfiguration({FeignAutoConfiguration.class})
@EnableKafka

public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }

}
