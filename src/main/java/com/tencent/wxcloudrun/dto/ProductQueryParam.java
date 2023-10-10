package com.tencent.wxcloudrun.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProductQueryParam extends PageParam {
  @ApiModelProperty(value = "pdd/tb:用于翻页时锁定唯一的商品列表，请求商品分页数=1时非必填，请求商品分页数>1时必填")
  private String listId;

  @ApiModelProperty(value = "来源：1->pdd; 2->jd; 3->tb; 4->dy; 5->wph")
  private Integer source;

  @ApiModelProperty(value = "pdd:商品类目ID; jd:频道ID")
  private Long optId;

  @ApiModelProperty(value = "pdd:频道推广")
  private Integer channelType;

  @ApiModelProperty(value = "wph:精选组货码")
  private String jxCode;

  @ApiModelProperty(value = "关键字")
  private String keyword;

  @ApiModelProperty(value = "0-综合排序;5-按销量升序;6-按销量降序;9-券后价升序排序;10-券后价降序排序")
  private Integer sortType = 0;

  @ApiModelProperty(value = "是否只返回优惠券的商品，false返回所有商品，true只返回有优惠券的商品")
  private Boolean withCoupon = true;

  @ApiModelProperty(value = "商品goodsSign，多个用英文逗号分隔")
  private String goodsIds;

  @ApiModelProperty(value = "是否是天猫")
  private Boolean isTmall;

  @ApiModelProperty(value = "用户id")
  private String uid;
}
