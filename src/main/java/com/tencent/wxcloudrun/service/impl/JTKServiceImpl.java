package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.config.properties.JTKProperties;
import com.tencent.wxcloudrun.dto.JTKLinkResponse;
import com.tencent.wxcloudrun.model.SysConfig;
import com.tencent.wxcloudrun.service.JTKService;
import com.tencent.wxcloudrun.service.SysConfigService;
import com.tencent.wxcloudrun.utils.JsonUtil;
import com.tencent.wxcloudrun.utils.RequestHolder;
import com.tencent.wxcloudrun.utils.RestTemplateUtil;
import com.tencent.wxcloudrun.utils.XLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class JTKServiceImpl implements JTKService {
  final Logger logger = LoggerFactory.getLogger(JTKServiceImpl.class);

  @Value("${spring.profiles.active}")
  private String env;

  @Resource private JTKProperties jtkProperties;
  @Resource private SysConfigService sysConfigService;

  @Override
  public Object getMeituanLink() {
    return sysConfigService.lambdaQuery().like(SysConfig::getCCode, "meituan_link").select().list();
  }

  @Override
  public Object getEleLink() {
    return sysConfigService.lambdaQuery().like(SysConfig::getCCode, "ele_link").select().list();
  }

  @Override
  public SysConfig getPhoneBillLink() {
    return sysConfigService.lambdaQuery().eq(SysConfig::getCCode, "phone_bill_link").one();
  }

  @Override
  public Object getUnionLink(Integer type) {
    int actId = 0;
    switch (type) {
      case 0:
        actId = 42; // 滴滴
        break;
      case 1:
        actId = 104; // T3
        break;
      case 2:
        actId = 49; // 花小猪
        break;
      case 3:
        actId = 76; // 电影
        break;
      case 4:
        actId = 54; // 周边
        break;
      case 5:
        actId = 105; // 景点
        break;
    }
    // 获取推广链接
    String api = jtkProperties.getApiUrl() + "/union/act";
    Map<String, Object> map =
        new HashMap<String, Object>() {
          {
            put("apikey", jtkProperties.getApiKey());
            put("sid", RequestHolder.getUid());
          }
        };
    map.put("act_id", actId);
    JTKLinkResponse response =
        RestTemplateUtil.getInstance().postForObject(api, map, JTKLinkResponse.class);

    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "聚推客活动转链",
        JsonUtil.toJson(map),
        JsonUtil.toJson(response));
    if (response != null && response.getData() != null) {
      return response.getData();
    }
    return null;
  }
}
