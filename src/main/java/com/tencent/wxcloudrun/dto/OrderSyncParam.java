package com.tencent.wxcloudrun.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/** 订单同步参数 */
@Data
public class OrderSyncParam {
  @ApiModelProperty(value = "开始时间")
  @NotBlank(message = "开始时间不能为空")
  private String startTime;

  @ApiModelProperty(value = "结束时间")
  private String endTime;
}
