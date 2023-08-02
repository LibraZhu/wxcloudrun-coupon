package com.tencent.wxcloudrun.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/** 订单同步参数 */
@Data
public class OrderSyncParam {
  @ApiModelProperty(value = "开始时间")
  @NotNull(message = "开始时间不能为空")
  private String startTime;

  @ApiModelProperty(value = "结束时间")
  private String endTime;
}
