package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.util.StrUtil;
import com.tencent.wxcloudrun.dto.WxMessageRequest;
import com.tencent.wxcloudrun.service.JDService;
import com.tencent.wxcloudrun.service.PddService;
import com.tencent.wxcloudrun.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserServiceImpl implements UserService {

    final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Resource
    private PddService pddService;
    @Resource
    private JDService jdService;

    @Value("${spring.profiles.active}")
    private String env;

    @Override
    public Object userMessage(WxMessageRequest request) {
        if (request != null && StrUtil.equals("text", request.getMsgType())) {
            if (request.getContent().startsWith("https://item.m.jd.com") || request.getContent().startsWith("https://item.jd.com")) {
                return jdService.wxMessage(request);
            } else if (request.getContent().contains("yangkeduo.com")) {
                return pddService.wxMessage(request);
            }
        } else if (request != null && StrUtil.equals("event", request.getMsgType()) && StrUtil.equals("click", request.getEvent())) {
            // 自定义菜单
        }
        return "success";
    }
}
