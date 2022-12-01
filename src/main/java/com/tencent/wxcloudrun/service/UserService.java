package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.common.api.CommonResult;
import com.tencent.wxcloudrun.dto.WxMenuRequest;
import com.tencent.wxcloudrun.dto.WxMessageRequest;

public interface UserService {
    Object userMessage(WxMessageRequest request);
    CommonResult userCreateMenu(WxMenuRequest request);
}
