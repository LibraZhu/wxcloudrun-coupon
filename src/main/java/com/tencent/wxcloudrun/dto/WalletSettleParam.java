package com.tencent.wxcloudrun.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/** 结算参数 */
@Data
public class WalletSettleParam {

  @ApiModelProperty(value = "订单号列表")
  @NotEmpty(message = "订单号不能为空")
  private List<String> orderIds;

  @ApiModelProperty(value = "结算年月")
  @NotBlank(message = "结算日期不能为空")
  private String period;

  @ApiModelProperty(value = "用户标识")
  @NotBlank(message = "用户不能为空")
  private String uid;

  @ApiModelProperty(value = "备注")
  private String remark;
}
