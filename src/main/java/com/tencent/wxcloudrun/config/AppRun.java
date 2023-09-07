package com.tencent.wxcloudrun.config;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.tencent.wxcloudrun.model.SysConfig;
import com.tencent.wxcloudrun.service.OmsOrderService;
import com.tencent.wxcloudrun.service.SysConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class AppRun implements ApplicationRunner {
  final Logger logger = LoggerFactory.getLogger(AppRun.class);
  @Resource SysConfigService sysConfigService;
  @Resource OmsOrderService orderService;

  @Override
  public void run(ApplicationArguments args) throws Exception {
//    String startTime =
//        sysConfigService
//            .lambdaQuery()
//            .eq(SysConfig::getCCode, "order_sync_time")
//            .oneOpt()
//            .map(SysConfig::getCValue)
//            .orElse("");
//    String endTime = DateUtil.formatDateTime(DateUtil.date());
//    // 如果有上一次同步时间则全量同步
//    if (ObjectUtil.isNotEmpty(startTime)) {
//      logger.info("全量同步订单，开始时间：{}，结束时间：{}", startTime, endTime);
//      orderService.syncOrder(startTime, endTime);
//    }
  }
}
