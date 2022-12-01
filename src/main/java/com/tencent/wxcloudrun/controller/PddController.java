package com.tencent.wxcloudrun.controller;

import cn.hutool.core.util.StrUtil;
import com.pdd.pop.sdk.common.util.JsonUtil;
import com.pdd.pop.sdk.http.PopClient;
import com.pdd.pop.sdk.http.PopHttpClient;
import com.pdd.pop.sdk.http.api.pop.request.PddDdkRpPromUrlGenerateRequest;
import com.pdd.pop.sdk.http.api.pop.response.PddDdkRpPromUrlGenerateResponse;
import com.tencent.wxcloudrun.common.api.CommonResult;
import com.tencent.wxcloudrun.config.properties.ClientProperties;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 多宝客api
 */
@Api(tags = "PddController", description = "多宝客api")
@RestController
public class PddController {

    final Logger logger;
    @Resource
    private ClientProperties clientProperties;
    @Value("${spring.profiles.active}")
    private String env;

    public PddController() {
        this.logger = LoggerFactory.getLogger(PddController.class);
    }

    private PopClient getClient() {
        return new PopHttpClient(
                clientProperties.getClientId(),
                clientProperties.getClientSecret());
    }


    @GetMapping(value = "/pdd")
    public Object pdd(Map<String, Object> request) {
        return "";
    }

}
