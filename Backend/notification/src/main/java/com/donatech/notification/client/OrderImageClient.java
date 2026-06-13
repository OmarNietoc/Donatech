package com.donatech.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service", path = "/api/orders", contextId = "orderImageClient")
public interface OrderImageClient {

    @GetMapping("/{id}/thank-you-image/{index}")
    byte[] getThankYouImage(@PathVariable("id") Long id, @PathVariable("index") int index);
}
