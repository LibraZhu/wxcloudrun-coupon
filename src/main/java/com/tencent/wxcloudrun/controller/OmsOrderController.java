package com.tencent.wxcloudrun.controller;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.common.api.CommonResult;
import com.tencent.wxcloudrun.dto.OrderQueryParam;
import com.tencent.wxcloudrun.dto.OrderSyncParam;
import com.tencent.wxcloudrun.model.OmsOrder;
import com.tencent.wxcloudrun.service.OmsOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
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

    @GetMapping("/list")
    @ResponseBody
    public CommonResult<CommonPage<OmsOrder>> list(OrderQueryParam queryParam) {
        QueryWrapper<OmsOrder> queryWrapper = new QueryWrapper<>();
        if (ObjectUtil.isNotEmpty(queryParam.getOrderId())) {
            queryWrapper.eq("order_id", queryParam.getOrderId());
        }
        if (ObjectUtil.isNotEmpty(queryParam.getOrderTime())) {
            queryWrapper.eq("order_time", queryParam.getOrderTime());
        }
        if (ObjectUtil.isNotEmpty(queryParam.getOrderSource())) {
            queryWrapper.eq("order_source", queryParam.getOrderSource());
        }
        if (ObjectUtil.isNotEmpty(queryParam.getStatus())) {
            queryWrapper.eq("status", queryParam.getStatus());
        }
        if (ObjectUtil.isNotEmpty(queryParam.getUid())) {
            queryWrapper.eq("uid", queryParam.getUid());
        }
        IPage<OmsOrder> orderPage = omsOrderService.page(new Page<>(queryParam.getPage(), queryParam.getPageSize()), queryWrapper);
        return CommonResult.success(CommonPage.page(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal(), orderPage.getRecords()));
    }

    @PostMapping("/sync")
    @ResponseBody
    public CommonResult<String> sync(@Validated @RequestBody OrderSyncParam queryParam) {
        omsOrderService.syncOrder(queryParam.getStartTime(), queryParam.getEndTime());
        return CommonResult.success();
    }

}

