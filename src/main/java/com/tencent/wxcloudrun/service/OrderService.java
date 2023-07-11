package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.dto.OrderListRequest;

public interface OrderService {
    Object list(OrderListRequest request);
}
