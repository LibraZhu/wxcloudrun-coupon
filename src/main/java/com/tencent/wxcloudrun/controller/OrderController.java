package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.dto.OrderListRequest;
import com.tencent.wxcloudrun.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 多宝客api
 */
@RestController
public class OrderController {

    OrderService orderService;

    public OrderController(@Autowired OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping(value = "/order/list")
    public Object list(OrderListRequest request) {
        return orderService.list(request);
    }

}
