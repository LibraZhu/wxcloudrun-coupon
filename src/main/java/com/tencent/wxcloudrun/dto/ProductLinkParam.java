package com.tencent.wxcloudrun.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/** 转链参数 */
@Data
public class ProductLinkParam {
  @ApiModelProperty(value = "京东商品ID或链接")
  @NotNull(message = "商品ID")
  private String productId;

  @ApiModelProperty(value = "优惠券链接")
  private String couponUrl;

  @ApiModelProperty(value = "用户标识")
  private String uid;
}
