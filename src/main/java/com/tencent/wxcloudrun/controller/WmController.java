package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.common.api.CommonResult;
import com.tencent.wxcloudrun.service.WMService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 外卖 前端控制器
 *
 * @author zjf
 * @since 2023年07月11日
 */
@RestController
@RequestMapping("/wm")
public class WmController {
  WMService wmService;

  public WmController(@Autowired WMService wmService) {
    this.wmService = wmService;
  }

  @ApiOperation(
      "美团联盟外卖/酒店/券包活动转链接口。1-外卖 6-酒店活动页 8-外卖品质商家活动 9-美团电商-嗨购节 10-美团电商-超级好物场 11-美团酒店搜索首页 15-美团券包 ")
  @GetMapping("/meituan")
  @ResponseBody
  public CommonResult<Object> meituan(@RequestParam Integer type) {
    return CommonResult.success(wmService.getMeituanLink(type));
  }

  @ApiOperation("饿了么外卖/生鲜红包活动转链接口。3-饿了么浏览店铺得红包活动（不支持h5推广链接，只支持小程序推广） 4-饿了么外卖活动 5-饿了么新零售（只支持小程序推广）")
  @GetMapping("/ele")
  @ResponseBody
  public CommonResult<Object> ele(@RequestParam Integer type) {
    return CommonResult.success(wmService.getEleLink(type));
  }
}
