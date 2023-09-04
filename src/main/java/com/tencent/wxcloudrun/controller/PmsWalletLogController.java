package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.common.api.CommonResult;
import com.tencent.wxcloudrun.dto.PageParam;
import com.tencent.wxcloudrun.model.PmsWalletLog;
import com.tencent.wxcloudrun.service.PmsWalletLogService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 钱包变动日志 前端控制器
 *
 * @author zjf
 * @since 2023年08月08日
 */
@RestController
@RequestMapping("/wallet/log")
public class PmsWalletLogController {

  PmsWalletLogService pmsWalletLogService;

  public PmsWalletLogController(@Autowired PmsWalletLogService pmsWalletLogService) {
    this.pmsWalletLogService = pmsWalletLogService;
  }

  @ApiOperation("钱包明细列表")
  @PostMapping("/list")
  @ResponseBody
  public CommonResult<CommonPage<PmsWalletLog>> list(@RequestBody PageParam request) {
    return CommonResult.success(pmsWalletLogService.listLog(request));
  }
}
