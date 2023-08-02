package com.tencent.wxcloudrun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tencent.wxcloudrun.model.OmsOrder;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author zjf
 * @since 2023年07月11日
 */
public interface OmsOrderService extends IService<OmsOrder> {
    /**
     * 同步订单
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    void syncOrder(String startTime, String endTime);
}
