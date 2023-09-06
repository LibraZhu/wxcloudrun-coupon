package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.config.properties.JTKProperties;
import com.tencent.wxcloudrun.dto.JTKELELinkResponse;
import com.tencent.wxcloudrun.dto.JTKLinkResponse;
import com.tencent.wxcloudrun.dto.JTKMTLinkResponse;
import com.tencent.wxcloudrun.service.JTKService;
import com.tencent.wxcloudrun.utils.RequestHolder;
import com.tencent.wxcloudrun.utils.RestTemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class JTKServiceImpl implements JTKService {
  final Logger logger = LoggerFactory.getLogger(JTKServiceImpl.class);

  @Resource private JTKProperties jtkProperties;

  @Override
  public Object getMeituanLink(Integer type) {
    // 获取推广链接
    String api = jtkProperties.getApiUrl() + "/Meituan/act";
    Map<String, Object> map =
        new HashMap<String, Object>() {
          {
            put("apikey", jtkProperties.getApiKey());
            put("type", type);
            put("sid", RequestHolder.getUid());
            put("channels", 1);
          }
        };
    JTKMTLinkResponse response =
        RestTemplateUtil.getInstance().postForObject(api, map, JTKMTLinkResponse.class);
    if (response != null && response.getData() != null) {
      return response.getData().getWeAppInfo();
    }
    return null;
  }

  @Override
  public Object getEleLink(Integer type) {
    // 获取推广链接
    String api = jtkProperties.getApiUrl() + "/Ele/act";
    Map<String, Object> map =
        new HashMap<String, Object>() {
          {
            put("apikey", jtkProperties.getApiKey());
            put("type", type);
            put("sid", RequestHolder.getUid());
          }
        };
    JTKELELinkResponse response =
        RestTemplateUtil.getInstance().postForObject(api, map, JTKELELinkResponse.class);
    if (response != null && response.getData() != null) {
      return response.getData();
    }
    return null;
  }

  @Override
  public Object getUnionLink(Integer type) {
    int actId = 0;
    switch (type) {
      case 0:
        actId = 42;
        break;
      case 1:
        actId = 104;
        break;
      case 2:
        actId = 49;
        break;
      case 3:
        actId = 54;
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
    if (response != null && response.getData() != null) {
      return response.getData();
    }
    return null;
  }
}
