package com.tencent.wxcloudrun.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 转链参数
 */
@Data
public class ProductLinkParam {
  @ApiModelProperty(value = "京东商品ID或链接")
  @NotNull(message = "商品ID不能为空")
  private String productId;

  @ApiModelProperty(value = "来源：1->pdd; 2->jd; 3->tb; 4->dy; 5->wph")
  @NotNull(message = "商品来源不能为空")
  private Integer source;

  @ApiModelProperty(value = "优惠券链接")
  private String couponUrl;

  @ApiModelProperty(value = "商品搜索id")
  private String searchId;
}
