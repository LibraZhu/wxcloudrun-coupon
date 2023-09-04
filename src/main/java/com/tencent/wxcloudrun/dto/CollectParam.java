package com.tencent.wxcloudrun.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 收藏列表参数 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CollectParam extends PageParam {
  @ApiModelProperty(value = "商品id")
  private String productId;

  @ApiModelProperty(value = "商品json")
  private String product;

  @ApiModelProperty(value = "是否收藏")
  private Boolean collect;
}
