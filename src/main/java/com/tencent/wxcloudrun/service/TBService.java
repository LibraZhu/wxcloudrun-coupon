package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.dto.WxMessageRequest;

public interface TBService {
    /**
     * 同步订单
     * @param startTime 开始时间
     */
    void syncOrder(String startTime);

    /**
     * 公众号链接转链回复
     * @param request 微信消息
     * @return 微信回复消息
     */
    Object wxMessage(WxMessageRequest request);
}
