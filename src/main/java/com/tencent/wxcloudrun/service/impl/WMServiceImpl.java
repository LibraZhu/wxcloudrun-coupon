package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.config.properties.JTKProperties;
import com.tencent.wxcloudrun.dto.JTKELELinkResponse;
import com.tencent.wxcloudrun.dto.JTKMTLinkResponse;
import com.tencent.wxcloudrun.service.WMService;
import com.tencent.wxcloudrun.utils.RestTemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class WMServiceImpl implements WMService {
  final Logger logger = LoggerFactory.getLogger(WMServiceImpl.class);

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
            put("sid", "ddk");
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
            put("sid", "ddk");
          }
        };
    JTKELELinkResponse response =
        RestTemplateUtil.getInstance().postForObject(api, map, JTKELELinkResponse.class);
    if (response != null && response.getData() != null) {
      return response.getData();
    }
    return null;
  }
}
