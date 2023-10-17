package com.tencent.wxcloudrun.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *  订单查询参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OrderQueryParam extends PageParam {
  @ApiModelProperty(value = "订单状态：0-已支付；1-确认收货；2-审核成功；3-审核失败（不可提现）；4-已经结算 ;5-已返现")
  private Integer status;

  @ApiModelProperty(value = "来源: 1->pdd; 2->jd; 3->tb")
  private Integer orderSource;
}
