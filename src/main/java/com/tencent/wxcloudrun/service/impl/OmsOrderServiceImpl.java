package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.common.exception.Asserts;
import com.tencent.wxcloudrun.dao.OmsOrderMapper;
import com.tencent.wxcloudrun.dto.OrderDto;
import com.tencent.wxcloudrun.dto.OrderQueryParam;
import com.tencent.wxcloudrun.model.OmsOrder;
import com.tencent.wxcloudrun.model.UmsUserTb;
import com.tencent.wxcloudrun.service.*;
import com.tencent.wxcloudrun.utils.RequestHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * 订单表 服务实现类
 *
 * @author zjf
 * @since 2023年07月11日
 */
@Service
public class OmsOrderServiceImpl extends ServiceImpl<OmsOrderMapper, OmsOrder>
    implements OmsOrderService {
  final Logger logger = LoggerFactory.getLogger(OmsOrderServiceImpl.class);

  @Resource private JDService jdService;
  @Resource private PddService pddService;
  @Resource private TBService tbService;
  @Resource private DYService dyService;
  @Resource private WPHService wphService;
  @Resource private UmsUserTbService umsUserTbService;

  @Value("${spring.profiles.active}")
  private String env;

  @Override
  public void syncTask(String startTime, String endTime) {
    jdService.syncTask(startTime, endTime);
    pddService.syncTask(startTime, endTime);
    tbService.syncTask(startTime, endTime);
    dyService.syncTask(startTime, endTime);
    wphService.syncTask(startTime, endTime);
  }

  @Override
  public void syncOrder(String startTime, String endTime) {
    jdService.syncOrder(startTime, endTime);
    pddService.syncOrder(startTime, endTime);
    tbService.syncOrder(startTime, endTime);
    dyService.syncOrder(startTime, endTime);
    wphService.syncOrder(startTime, endTime);
  }

  @Override
  public CommonPage<OrderDto> list(OrderQueryParam queryParam) {

    String uid = RequestHolder.getUid();
    if (ObjectUtil.isEmpty(uid)) {
      Asserts.fail("用户不存在");
    }
    String rid =
        umsUserTbService
            .lambdaQuery()
            .eq(UmsUserTb::getUid, uid)
            .oneOpt()
            .map(UmsUserTb::getRelationId)
            .orElse(null);
    LambdaQueryChainWrapper<OmsOrder> queryWrapper = lambdaQuery();
    if (ObjectUtil.isNotEmpty(queryParam.getOrderSource())) {
      queryWrapper.eq(OmsOrder::getOrderSource, queryParam.getOrderSource());
    }
    if (ObjectUtil.isNotEmpty(queryParam.getStatus())) {
      queryWrapper.eq(OmsOrder::getStatus, queryParam.getStatus());
    } else {
      queryWrapper.isNotNull(OmsOrder::getStatus);
    }
    if (ObjectUtil.isEmpty(rid)) {
      queryWrapper.eq(OmsOrder::getUid, uid);
    } else {
      queryWrapper.and(wrapper -> wrapper.eq(OmsOrder::getUid, uid).or().eq(OmsOrder::getUid, rid));
    }
    queryWrapper.orderByDesc(OmsOrder::getOrderTime);
    Page<OmsOrder> orderPage =
        queryWrapper.page(Page.of(queryParam.getPage(), queryParam.getPageSize()));
    return CommonPage.page(
        orderPage.getCurrent(),
        orderPage.getSize(),
        orderPage.getTotal(),
        orderPage.getRecords().stream()
            .map(
                order -> {
                  OrderDto orderDto = new OrderDto();
                  orderDto.setId(order.getId());
                  orderDto.setOrderId(order.getOrderId());
                  orderDto.setOrderSn(order.getOrderSn());
                  orderDto.setOrderSource(order.getOrderSource());
                  orderDto.setOrderEmt(order.getOrderEmt());
                  orderDto.setOrderTime(order.getOrderTime());
                  orderDto.setModifyTime(order.getModifyTime());
                  orderDto.setFinishTime(order.getFinishTime());
                  orderDto.setSettlementTime(order.getSettleTime());
                  orderDto.setPlus(order.getPlus());
                  orderDto.setCompared(order.getCompared());
                  orderDto.setSkuId(order.getSkuId());
                  orderDto.setSkuName(order.getSkuName());
                  orderDto.setSkuNum(order.getSkuNum());
                  orderDto.setImageUrl(order.getImageUrl());
                  orderDto.setPrice(order.getPrice());
                  orderDto.setPriceTotal(
                      new BigDecimal(order.getPrice())
                          .multiply(new BigDecimal(order.getSkuNum()))
                          .toString());
                  orderDto.setRebate(order.getRebate());
                  orderDto.setStatus(order.getStatus());
                  orderDto.setUid(order.getUid());
                  orderDto.setStatusDes(order.getStatusDes());
                  return orderDto;
                })
            .collect(Collectors.toList()));
  }
}
