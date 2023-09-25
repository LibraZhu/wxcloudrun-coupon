package com.tencent.wxcloudrun.controller;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.tencent.wxcloudrun.common.api.CommonResult;
import com.tencent.wxcloudrun.dto.UserInfoParam;
import com.tencent.wxcloudrun.dto.WxMessageRequest;
import com.tencent.wxcloudrun.model.UmsUser;
import com.tencent.wxcloudrun.service.UmsUserService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户表 前端控制器
 *
 * @author zjf
 * @since 2023年08月02日
 */
@RestController
@RequestMapping("/user")
public class UmsUserController {
  final Logger logger = LoggerFactory.getLogger(UmsUserController.class);

  UmsUserService umsUserService;

  public UmsUserController(@Autowired UmsUserService umsUserService) {
    this.umsUserService = umsUserService;
  }

  @ApiOperation("小程序用户登录")
  @PostMapping(value = "/login")
  @ResponseBody
  public CommonResult<UmsUser> login(@RequestHeader Map<String, String> headers) {
    return CommonResult.success(
        umsUserService.login(headers.get("x-wx-from-openid"), headers.get("x-wx-from-unionid")));
  }

  @ApiOperation("公众号用户登录")
  @PostMapping(value = "/loginG")
  @ResponseBody
  public CommonResult<UmsUser> loginG(@RequestBody Map<String, String> request) {
    return CommonResult.success(
        umsUserService.loginG(request.get("openid"), request.get("gOpenid")));
  }

  @ApiOperation("用户信息更新")
  @PostMapping(value = "/update")
  @ResponseBody
  public CommonResult<Object> update(@Validated @RequestBody UserInfoParam request) {
    return CommonResult.success(umsUserService.update(request));
  }

  @ApiOperation("微信公众号消息处理")
  @PostMapping(value = "/message")
  @ResponseBody
  public Object userMessage(@RequestBody WxMessageRequest request) {
    return umsUserService.userMessage(request);
  }
}
