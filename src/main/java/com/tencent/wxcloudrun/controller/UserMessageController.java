package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.common.api.CommonResult;
import com.tencent.wxcloudrun.dto.WxMenuRequest;
import com.tencent.wxcloudrun.dto.WxMessageRequest;
import com.tencent.wxcloudrun.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户消息控制器
 */
@Api(tags = "UserMessageController", description = "用户消息管理")
@RestController
public class UserMessageController {

    UserService userService;

    public UserMessageController(@Autowired UserService userService) {
        this.userService = userService;
    }


    /**
     * 消息推送
     *
     * @param request {@link WxMessageRequest}
     * @return API response json
     */
    @ApiOperation("微信公众号消息处理")
    @PostMapping(value = "/user/message")
    public Object userMessage(@RequestBody WxMessageRequest request) {
        return userService.userMessage(request);
    }
    /**
     * 创建菜单
     *
     * @param request {@link WxMessageRequest}
     * @return API response json
     */
    @ApiOperation("微信公众号创建菜单")
    @PostMapping(value = "/user/createMenu")
    public CommonResult createMenu(@RequestBody WxMenuRequest request) {
        return userService.userCreateMenu(request);
    }
}