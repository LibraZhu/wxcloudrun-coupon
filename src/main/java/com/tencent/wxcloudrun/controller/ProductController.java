package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.common.api.CommonResult;
import com.tencent.wxcloudrun.dto.*;
import com.tencent.wxcloudrun.service.JDService;
import com.tencent.wxcloudrun.service.OmsOrderService;
import com.tencent.wxcloudrun.service.PddService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单表 前端控制器
 *
 * @author zjf
 * @since 2023年07月11日
 */
@RestController
@RequestMapping("/product")
public class ProductController {
  JDService jdService;
  PddService pddService;
  OmsOrderService omsOrderService;

  public ProductController(
      @Autowired JDService jdService,
      @Autowired PddService pddService,
      @Autowired OmsOrderService omsOrderService) {
    this.jdService = jdService;
    this.pddService = pddService;
    this.omsOrderService = omsOrderService;
  }

  @PostMapping("/sync")
  @ResponseBody
  public CommonResult<String> sync(@RequestBody OrderSyncParam queryParam) {
    omsOrderService.syncOrder(queryParam.getStartTime(), queryParam.getEndTime());
    return CommonResult.success();
  }

  @GetMapping("/list/jd")
  @ResponseBody
  public CommonResult<CommonPage<HJKJDProduct>> list(@Validated JDProductListParam queryParam) {
    return CommonResult.success(jdService.listProduct(queryParam));
  }

  @GetMapping("/search/jd")
  @ResponseBody
  public CommonResult<CommonPage<HJKJDProduct>> searchJD(HJKJDProductQueryParam queryParam) {
    return CommonResult.success(jdService.searchHJKProduct(queryParam));
  }

  @GetMapping("/search/pdd")
  @ResponseBody
  public CommonResult<CommonPage<HJKJDProduct>> searchPdd(PddProductQueryParam queryParam) {
    return CommonResult.success(pddService.searchProduct(queryParam));
  }

  @PostMapping("/link/jd")
  @ResponseBody
  public CommonResult<String> linkJD(@RequestBody ProductLinkParam queryParam) {
    return CommonResult.success(
        jdService.getHDKUnionUrl(
            queryParam.getProductId(), queryParam.getCouponUrl(), queryParam.getUid()));
  }
}
