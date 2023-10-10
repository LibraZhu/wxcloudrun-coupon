package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.common.api.CommonResult;
import com.tencent.wxcloudrun.service.JTKService;
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
@RequestMapping("/jtk")
public class JtkController {
  JTKService wmService;

  public JtkController(@Autowired JTKService wmService) {
    this.wmService = wmService;
  }

  @ApiOperation(
      "美团联盟外卖/券包活动转链接口。")
  @GetMapping("/meituan")
  @ResponseBody
  public CommonResult<Object> meituan() {
    return CommonResult.success(wmService.getMeituanLink());
  }

  @ApiOperation("饿了么外卖/红包推广链接。")
  @GetMapping("/ele")
  @ResponseBody
  public CommonResult<Object> ele() {
    return CommonResult.success(wmService.getEleLink());
  }

  @ApiOperation("话费充值推广链接。")
  @GetMapping("/phoneBill")
  @ResponseBody
  public CommonResult<Object> phoneBill() {
    return CommonResult.success(wmService.getPhoneBillLink());
  }

  @ApiOperation("统一活动转链接口。0-滴滴打车；1-T3; 2-花小猪; 3-电影")
  @GetMapping("/unionLink")
  @ResponseBody
  public CommonResult<Object> unionLink(@RequestParam Integer type) {
    return CommonResult.success(wmService.getUnionLink(type));
  }
}
