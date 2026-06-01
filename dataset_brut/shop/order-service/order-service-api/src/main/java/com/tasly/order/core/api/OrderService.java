package com.tasly.order.core.api;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by dulei on 18/1/9.
 */
@FeignClient(value = "order-service")
public interface OrderService {

    @RequestMapping(value = "/",method = RequestMethod.GET)
    String placeOrder(@RequestParam("score") Integer score,@RequestParam("sku") String sku);

}
