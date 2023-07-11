package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.util.StrUtil;
import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.domain.kplunion.OrderService.request.query.OrderReq;
import com.jd.open.api.sdk.domain.kplunion.OrderService.request.query.OrderRowReq;
import com.jd.open.api.sdk.request.kplunion.UnionOpenOrderQueryRequest;
import com.jd.open.api.sdk.request.kplunion.UnionOpenOrderRowQueryRequest;
import com.jd.open.api.sdk.response.kplunion.UnionOpenOrderQueryResponse;
import com.jd.open.api.sdk.response.kplunion.UnionOpenOrderRowQueryResponse;
import com.pdd.pop.sdk.common.util.JsonUtil;
import com.tencent.wxcloudrun.config.properties.JDProperties;
import com.tencent.wxcloudrun.dto.OrderListRequest;
import com.tencent.wxcloudrun.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class OrderServiceImpl implements OrderService {

    final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Resource
    private JDProperties jdProperties;

    @Value("${spring.profiles.active}")
    private String env;

    private DefaultJdClient getJDClient() {
        return new DefaultJdClient(jdProperties.getServerUrl(), null, jdProperties.getAppKey(), jdProperties.getAppSecret());
    }

    @Override
    public Object list(OrderListRequest request) {
        try {
            UnionOpenOrderRowQueryRequest orderRowQueryRequest=new UnionOpenOrderRowQueryRequest();
            OrderRowReq orderReq=new OrderRowReq();
            orderReq.setType(3);
            orderReq.setPageSize(1);
            orderReq.setPageSize(100);
            orderReq.setStartTime("2023-07-02 19:00:00");
            orderReq.setEndTime("2023-07-02 20:00:00");
            orderRowQueryRequest.setOrderReq(orderReq);
            orderRowQueryRequest.setVersion("1.0");
            UnionOpenOrderRowQueryResponse response=getJDClient().execute(orderRowQueryRequest);
            if (StrUtil.equals(env, "dev")) {
                logger.info("Method:[{}],Request:{},Response:{}", "Order List", JsonUtil.transferToJson(orderReq), JsonUtil.transferToJson(response.getQueryResult()));
            }
            return response.getQueryResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
