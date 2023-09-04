package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class HDKDYProductResponse {

  @JsonProperty("code")
  private Integer code;
  @JsonProperty("msg")
  private String msg;
  @JsonProperty("data")
  private List<DataDTO> data;

  @NoArgsConstructor
  @Data
  public static class DataDTO {

    @JsonProperty("itemid")
    private String itemid;
    @JsonProperty("itemtitle")
    private String itemtitle;
    @JsonProperty("item_pic")
    private String itemPic;
    @JsonProperty("detail_url")
    private String detailUrl;
    @JsonProperty("price")
    private String price;
    @JsonProperty("max_price")
    private String maxPrice;
    @JsonProperty("month_sales")
    private String monthSales;
    @JsonProperty("shop_id")
    private String shopId;
    @JsonProperty("shop_name")
    private String shopName;
    @JsonProperty("shop_score")
    private String shopScore;
    @JsonProperty("dymoney")
    private String dymoney;
    @JsonProperty("dyrates")
    private String dyrates;
    @JsonProperty("sales")
    private String sales;
    @JsonProperty("cate_name")
    private String cateName;
    @JsonProperty("cate_id")
    private String cateId;
    @JsonProperty("end_price")
    private String endPrice;
  }
}
