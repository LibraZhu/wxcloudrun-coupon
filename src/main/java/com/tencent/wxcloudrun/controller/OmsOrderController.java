package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.common.api.CommonResult;
import com.tencent.wxcloudrun.dto.OrderDto;
import com.tencent.wxcloudrun.dto.OrderQueryParam;
import com.tencent.wxcloudrun.dto.OrderSyncParam;
import com.tencent.wxcloudrun.service.OmsOrderService;
import io.swagger.annotations.ApiOperation;
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
@RequestMapping("/order")
public class OmsOrderController {
  OmsOrderService omsOrderService;

  public OmsOrderController(@Autowired OmsOrderService omsOrderService) {
    this.omsOrderService = omsOrderService;
  }

  @ApiOperation("订单列表")
  @PostMapping("/list")
  @ResponseBody
  public CommonResult<CommonPage<OrderDto>> list(@RequestBody OrderQueryParam queryParam) {
    return CommonResult.success(omsOrderService.list(queryParam));
  }

  @ApiOperation("订单同步")
  @PostMapping("/sync")
  @ResponseBody
  public CommonResult<String> sync(@Validated @RequestBody OrderSyncParam queryParam) {
    omsOrderService.syncOrder(queryParam.getStartTime(), queryParam.getEndTime());
    return CommonResult.success();
  }
}
