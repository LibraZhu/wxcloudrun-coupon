package com.tencent.wxcloudrun.utils;

import cn.hutool.crypto.digest.DigestUtil;

import java.util.Map;

public class DTKUtil {
  public static Map<String, Object> sign(Map<String, Object> map) {
    String appKey = "65145031d9648";
    String appSecret = "f61f86d72c5a17e4be46ca605a103236";
    long timer = System.currentTimeMillis();
    int nonce = (int) ((Math.random() * 9 + 1) * 100000);
    String signRan =
        DigestUtil.md5Hex(
                String.format(
                    "appKey=%s&timer=%d&nonce=%d&key=%s", appKey, timer, nonce, appSecret))
            .toUpperCase();
    map.put("appKey", appKey);
    map.put("nonce", nonce);
    map.put("timer", timer);
    map.put("version", "1.0.0");
    map.put("signRan", signRan);
    return map;
  }
}
