package com.demo.feign;

import com.demo.dto.User;
import com.demo.config.openfeign.CommonOpenFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@FeignClient(
    name = "account-service", 
    url = "http://account-service-app:8088", 
    configuration = {CommonOpenFeignConfig.class}
)
public interface UserFeignClient {

    @GetMapping("/api/user/{id}")
    User getAccountInfo(@PathVariable Long id);

}
