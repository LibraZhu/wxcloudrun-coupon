package com.tencent.wxcloudrun.config;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.tencent.wxcloudrun.model.SysConfig;
import com.tencent.wxcloudrun.service.OmsOrderService;
import com.tencent.wxcloudrun.service.SysConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
public class ScheduledConfig implements SchedulingConfigurer {
  final Logger logger = LoggerFactory.getLogger(ScheduledConfig.class);
  private final String syncIntervalCron = "0 */5 * * * ?"; // 同步间隔时间表达式
  @Resource SysConfigService sysConfigService;
  @Resource OmsOrderService orderService;

  private final Map<String, String> syncTaskTimeMap = new ConcurrentHashMap<>();
  private final String orderSyncTime = "order_sync_time";

  @Override
  public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
    String runStartTime = DateUtil.formatDateTime(DateUtil.date());
    taskRegistrar.setScheduler(Executors.newSingleThreadScheduledExecutor());
    taskRegistrar.addTriggerTask(
        () -> {
          // 同步订单
          String startTime = syncTaskTimeMap.get(orderSyncTime);
          String endTime = DateUtil.formatDateTime(DateUtil.date());
          // 程序启动，读取上一次更新时间进行同步订单
          if (ObjectUtil.isEmpty(startTime)) {
            startTime = runStartTime;
          }
          logger.info("定时同步订单，开始时间：{}，结束时间：{}", startTime, endTime);
          orderService.syncTask(startTime, endTime);
          syncTaskTimeMap.put(orderSyncTime, endTime);
          sysConfigService
              .lambdaUpdate()
              .eq(SysConfig::getCCode, orderSyncTime)
              .set(SysConfig::getCValue, endTime)
              .update();
        },
        triggerContext -> new CronTrigger(syncIntervalCron).nextExecutionTime(triggerContext));
  }
}
