package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.model.SysConfig;

public interface JTKService {

  Object getMeituanLink();

  Object getEleLink();

  SysConfig getPhoneBillLink();

  Object getUnionLink(Integer type);
}
