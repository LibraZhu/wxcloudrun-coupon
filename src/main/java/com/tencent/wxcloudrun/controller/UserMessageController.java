package com.tencent.wxcloudrun.controller;

import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * counter控制器
 */
@RestController

public class UserMessageController {

    final Logger logger;

    public UserMessageController() {
        this.logger = LoggerFactory.getLogger(UserMessageController.class);
    }


    /**
     * 消息推送
     *
     * @return API response json
     */
    @GetMapping(value = "/user/message")
    Object userMessage(Map<String, Object> params) {
        logger.info("/user/message get request" + JSON.toJSONString(params));

        return "";
    }
}