package com.tencent.wxcloudrun.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/** 提现参数 */
@Data
public class WalletCashParam {

  @ApiModelProperty(value = "结算年月")
  @NotBlank(message = "提现金额不能为空")
  private String money;

  @ApiModelProperty(value = "提现账户")
  @NotBlank(message = "提现账户不能为空")
  private String account;

  @ApiModelProperty(value = "用户标识")
  @NotBlank(message = "用户不能为空")
  private String uid;

  @ApiModelProperty(value = "用户标识")
  @NotNull(message = "交易方式不能为空")
  private Integer payType;
}
