package com.tencent.wxcloudrun.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/** 钱包参数 */
@Data
public class WalletParam {
  @ApiModelProperty(value = "用户标识")
  @NotBlank(message = "用户不能为空")
  private String uid;
  @ApiModelProperty(value = "银行卡")
  private String bank;
  @ApiModelProperty(value = "银行名称")
  private String bankName;
  @ApiModelProperty(value = "真实名字")
  private String name;
  @ApiModelProperty(value = "微信号")
  private String weixin;
}
