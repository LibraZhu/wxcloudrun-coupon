package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class HDKDYProductDetailResponse {

  @JsonProperty("code")
  private Integer code;
  @JsonProperty("msg")
  private String msg;
  @JsonProperty("data")
  private List<DataDTO> data;

  @NoArgsConstructor
  @Data
  public static class DataDTO {
    @JsonProperty("product_id")
    private String productId;
    @JsonProperty("product_title")
    private String productTitle;
    @JsonProperty("price")
    private Double price;
    @JsonProperty("dymoney")
    private Double dymoney;
    @JsonProperty("dyrates")
    private Double dyrates;
    @JsonProperty("sales")
    private Integer sales;
    @JsonProperty("item_pic")
    private String itemPic;
    @JsonProperty("douyin_images")
    private List<String> douyinImages;
    @JsonProperty("detail_url")
    private String detailUrl;
    @JsonProperty("shop_id")
    private String shopId;
    @JsonProperty("shop_name")
    private String shopName;
    @JsonProperty("comment_num")
    private Integer commentNum;
    @JsonProperty("comment_score")
    private Double commentScore;
    @JsonProperty("order_num")
    private Integer orderNum;
    @JsonProperty("view_num")
    private Integer viewNum;
    @JsonProperty("kol_num")
    private Integer kolNum;
    @JsonProperty("logistics_info")
    private String logisticsInfo;
    @JsonProperty("coupon_price")
    private Integer couponPrice;
    @JsonProperty("end_price")
    private Double endPrice;
    @JsonProperty("sharable")
    private String sharable;
  }
}
