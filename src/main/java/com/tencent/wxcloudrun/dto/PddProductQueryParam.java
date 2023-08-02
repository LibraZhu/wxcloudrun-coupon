package com.tencent.wxcloudrun.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PddProductQueryParam extends PageParam {
  @ApiModelProperty(value = "用于翻页时锁定唯一的商品列表，请求商品分页数=1时非必填，请求商品分页数>1时必填")
  private String listId;

  @ApiModelProperty(value = "商品类目ID")
  private Long optId;

  @ApiModelProperty(value = "关键字")
  private String keyword;

  @ApiModelProperty(value = "0-综合排序;3-按价格升序;4-按价格降序;5-按销量升序;6-按销量降序;9-券后价升序排序;10-券后价降序排序")
  private Integer sortType = 0;

  @ApiModelProperty(value = "是否只返回优惠券的商品，false返回所有商品，true只返回有优惠券的商品")
  private Boolean withCoupon = true;
}
