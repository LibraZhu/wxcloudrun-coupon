package com.tencent.wxcloudrun.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/** 提现参数 */
@Data
public class WalletCashConfirmParam {
  @ApiModelProperty(value = "流水号")
  @NotBlank(message = "流水号不能为空")
  private String recordSn;

  @ApiModelProperty(value = "备注")
  private String remark;
}
