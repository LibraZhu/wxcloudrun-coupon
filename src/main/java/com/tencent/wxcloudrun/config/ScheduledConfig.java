package com.tencent.wxcloudrun.config;

import com.tencent.wxcloudrun.enums.ProductSource;
import com.tencent.wxcloudrun.service.JDService;
import com.tencent.wxcloudrun.service.PddService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableScheduling
public class ScheduledConfig implements SchedulingConfigurer {
    final Logger logger = LoggerFactory.getLogger(ScheduledConfig.class);
    private final int syncInterval = 2;//同步间隔时间，单位分钟
    private final String syncIntervalCron = "0 */2 * * * ?";//同步间隔时间表达式
    @Resource
    JDService jdService;
    @Resource
    PddService pddService;

    private final Map<ProductSource, String> syncTaskTimeMap = new ConcurrentHashMap<>();

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
//        taskRegistrar.addTriggerTask(() -> {
//            // 同步jd
//            String startTime = syncTaskTimeMap.get(OrderSource.JD);
//            String endTime;
//            // 第一次同步十分钟之前的订单，后续每${SYNC_INTERVAL}分钟同步一次
//            if (ObjectUtil.isEmpty(startTime)) {
//                DateTime now = DateUtil.date();
//                startTime = DateUtil.formatDateTime(DateUtil.offsetMinute(now, -10));
//                endTime = DateUtil.formatDateTime(now);
//            } else {
//                endTime = DateUtil.formatDateTime(DateUtil.offsetMinute(DateUtil.parseDateTime(startTime), syncInterval));
//            }
//            jdService.syncTask(startTime, endTime);
//            syncTaskTimeMap.put(OrderSource.JD, endTime);
//        }, triggerContext -> new CronTrigger(syncIntervalCron).nextExecutionTime(triggerContext));
//        taskRegistrar.addTriggerTask(() -> {
//            // 同步pdd
//            String startTime = syncTaskTimeMap.get(OrderSource.PDD);
//            String endTime;
//            // 第一次同步十分钟之前的订单，后续每${SYNC_INTERVAL}分钟同步一次
//            if (ObjectUtil.isEmpty(startTime)) {
//                DateTime now = DateUtil.date();
//                startTime = DateUtil.formatDateTime(DateUtil.offsetMinute(now, -10));
//                endTime = DateUtil.formatDateTime(now);
//            } else {
//                endTime = DateUtil.formatDateTime(DateUtil.offsetMinute(DateUtil.parseDateTime(startTime), syncInterval));
//            }
//            pddService.syncTask(startTime, endTime);
//            syncTaskTimeMap.put(OrderSource.PDD, endTime);
//        }, triggerContext -> new CronTrigger(syncIntervalCron).nextExecutionTime(triggerContext));
    }
}
