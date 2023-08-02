package com.tencent.wxcloudrun.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tencent.wxcloudrun.dao.OmsOrderMapper;
import com.tencent.wxcloudrun.model.OmsOrder;
import com.tencent.wxcloudrun.service.JDService;
import com.tencent.wxcloudrun.service.OmsOrderService;
import com.tencent.wxcloudrun.service.PddService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2023年07月11日
 */
@Service
public class OmsOrderServiceImpl extends ServiceImpl<OmsOrderMapper, OmsOrder> implements OmsOrderService {
    final Logger logger = LoggerFactory.getLogger(OmsOrderServiceImpl.class);

    @Resource
    private JDService jdService;
    @Resource
    private PddService pddService;

    @Value("${spring.profiles.active}")
    private String env;

    @Override
    public void syncOrder(String startTime, String endTime) {
        jdService.syncOrder(startTime, endTime);
        pddService.syncOrder(startTime, endTime);
    }
}
