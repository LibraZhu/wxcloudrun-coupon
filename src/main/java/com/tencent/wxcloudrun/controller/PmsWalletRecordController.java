package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.common.api.CommonResult;
import com.tencent.wxcloudrun.dto.PageParam;
import com.tencent.wxcloudrun.dto.WalletCashConfirmParam;
import com.tencent.wxcloudrun.dto.WalletCashParam;
import com.tencent.wxcloudrun.dto.WalletSettleParam;
import com.tencent.wxcloudrun.model.PmsWalletRecord;
import com.tencent.wxcloudrun.service.PmsWalletRecordService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 钱包交易记录表 前端控制器
 *
 * @author zjf
 * @since 2023年08月08日
 */
@RestController
@RequestMapping("/wallet/record")
public class PmsWalletRecordController {

  PmsWalletRecordService pmsWalletRecordService;

  public PmsWalletRecordController(@Autowired PmsWalletRecordService pmsWalletRecordService) {
    this.pmsWalletRecordService = pmsWalletRecordService;
  }

  @ApiOperation("结算")
  @PostMapping("/settle")
  @ResponseBody
  public CommonResult<Object> settle(@Validated @RequestBody WalletSettleParam request) {
    return CommonResult.success(pmsWalletRecordService.settle(request));
  }

  @ApiOperation("提现")
  @PostMapping("/cash")
  @ResponseBody
  public CommonResult<Object> cash(@Validated @RequestBody WalletCashParam request) {
    return CommonResult.success(pmsWalletRecordService.cash(request));
  }

  @ApiOperation("提现成功")
  @PostMapping("/cash/confirm")
  @ResponseBody
  public CommonResult<Object> cashConfirm(@Validated @RequestBody WalletCashConfirmParam request) {
    return CommonResult.success(pmsWalletRecordService.cashConfirm(request));
  }

  @ApiOperation("提现失败")
  @PostMapping("/cash/fail")
  @ResponseBody
  public CommonResult<Object> cashFail(@Validated @RequestBody WalletCashConfirmParam request) {
    return CommonResult.success(pmsWalletRecordService.cashFail(request));
  }

  @ApiOperation("结算列表")
  @PostMapping("/settleList")
  @ResponseBody
  public CommonResult<CommonPage<PmsWalletRecord>> settleList(@RequestBody PageParam request) {
    return CommonResult.success(pmsWalletRecordService.settleList(request));
  }

  @ApiOperation("提现列表")
  @PostMapping("/cashList")
  @ResponseBody
  public CommonResult<CommonPage<PmsWalletRecord>> cashList(@RequestBody PageParam request) {
    return CommonResult.success(pmsWalletRecordService.cashList(request));
  }
}
