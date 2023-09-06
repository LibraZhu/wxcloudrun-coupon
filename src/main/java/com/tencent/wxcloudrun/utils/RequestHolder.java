package com.tencent.wxcloudrun.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

public class RequestHolder {
  /**
   * 获取请求头里的uid
   * @return
   */
  public static String getUid() {
    ServletRequestAttributes requestAttributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    return Optional.ofNullable(requestAttributes)
        .map(ServletRequestAttributes::getRequest)
        .map(request -> request.getHeader("uid"))
        .orElse("");
  }
}
