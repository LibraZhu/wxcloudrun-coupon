package com.tencent.wxcloudrun.controller;

import cn.hutool.core.util.ObjectUtil;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.common.api.CommonResult;
import com.tencent.wxcloudrun.dto.CollectParam;
import com.tencent.wxcloudrun.dto.HJKJDProduct;
import com.tencent.wxcloudrun.service.CmsCollectService;
import com.tencent.wxcloudrun.service.UmsUserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 收藏表 前端控制器
 *
 * @author zjf
 * @since 2023年08月02日
 */
@RestController
@RequestMapping("/collect")
public class CmsCollectController {

  UmsUserService umsUserService;
  CmsCollectService cmsCollectService;

  public CmsCollectController(
      @Autowired CmsCollectService cmsCollectService, @Autowired UmsUserService umsUserService) {
    this.cmsCollectService = cmsCollectService;
    this.umsUserService = umsUserService;
  }

  @ApiOperation("商品是否收藏")
  @PostMapping(value = "/check")
  @ResponseBody
  public CommonResult<Boolean> check(
      @RequestHeader Map<String, String> headers, @RequestBody CollectParam request) {
    String uid = request.getUid();
    // 如果没有uid参数，先登录获取uid
    if (ObjectUtil.isEmpty(uid)) {
      uid = umsUserService.login(headers.get("x-wx-from-openid"), headers.get("x-wx-from-unionid")).getId().toString();
      request.setUid(uid);
    }
    return CommonResult.success(cmsCollectService.check(request));
  }

  @ApiOperation("商品收藏列表")
  @PostMapping("/list")
  @ResponseBody
  public CommonResult<CommonPage<HJKJDProduct>> list(@RequestBody CollectParam request) {
    return CommonResult.success(cmsCollectService.listCollect(request));
  }

  @ApiOperation("商品收藏操作")
  @PostMapping("/handle")
  @ResponseBody
  public CommonResult<Object> handle(@RequestBody CollectParam request) {
    return CommonResult.success(cmsCollectService.handle(request));
  }
}
