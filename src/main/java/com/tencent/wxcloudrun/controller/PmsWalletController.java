package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.common.api.CommonResult;
import com.tencent.wxcloudrun.dto.WalletMoneyDto;
import com.tencent.wxcloudrun.dto.WalletParam;
import com.tencent.wxcloudrun.model.PmsWallet;
import com.tencent.wxcloudrun.service.PmsWalletService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户钱包 前端控制器
 *
 * @author zjf
 * @since 2023年08月08日
 */
@RestController
@RequestMapping("/wallet")
public class PmsWalletController {

  PmsWalletService pmsWalletService;

  public PmsWalletController(@Autowired PmsWalletService pmsWalletService) {
    this.pmsWalletService = pmsWalletService;
  }

  @ApiOperation("钱包信息")
  @PostMapping("/number")
  @ResponseBody
  public CommonResult<WalletMoneyDto> number(@Validated @RequestBody WalletParam request) {
    return CommonResult.success(pmsWalletService.number(request));
  }

  @ApiOperation("钱包信息")
  @PostMapping("/info")
  @ResponseBody
  public CommonResult<PmsWallet> info(@Validated @RequestBody WalletParam request) {
    return CommonResult.success(pmsWalletService.info(request));
  }

  @ApiOperation("钱包信息")
  @PostMapping("/info/modify")
  @ResponseBody
  public CommonResult<Object> modify(@Validated @RequestBody WalletParam request) {
    return CommonResult.success(pmsWalletService.modify(request));
  }
}
