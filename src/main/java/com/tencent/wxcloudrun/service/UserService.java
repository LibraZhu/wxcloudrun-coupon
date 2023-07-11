package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.dto.WxMessageRequest;

public interface UserService {
    Object userMessage(WxMessageRequest request);
    Object pddAuthUrl();
}
