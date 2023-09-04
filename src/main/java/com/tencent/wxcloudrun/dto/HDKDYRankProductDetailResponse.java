package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class HDKDYRankProductDetailResponse {

  @JsonProperty("code")
  private Integer code;
  @JsonProperty("msg")
  private String msg;
  @JsonProperty("data")
  private List<DataDTO> data;
  @JsonProperty("min_id")
  private Integer minId;

  @NoArgsConstructor
  @Data
  public static class DataDTO {
    @JsonProperty("product_id")
    private String productId;
    @JsonProperty("itemtitle")
    private String itemtitle;
    @JsonProperty("itemshorttitle")
    private String itemshorttitle;
    @JsonProperty("shop_id")
    private String shopId;
    @JsonProperty("shop_name")
    private String shopName;
    @JsonProperty("shop_logo")
    private String shopLogo;
    @JsonProperty("shop_score")
    private String shopScore;
    @JsonProperty("item_score")
    private String itemScore;
    @JsonProperty("logistics_score")
    private String logisticsScore;
    @JsonProperty("shop_itemsale")
    private String shopItemsale;
    @JsonProperty("itemprice")
    private String itemprice;
    @JsonProperty("itemendprice")
    private String itemendprice;
    @JsonProperty("tkrates")
    private String tkrates;
    @JsonProperty("tkmoney")
    private String tkmoney;
    @JsonProperty("itempic")
    private String itempic;
    @JsonProperty("product_category_id")
    private String productCategoryId;
    @JsonProperty("sales")
    private String sales;
    @JsonProperty("dymoney")
    private String dymoney;
    @JsonProperty("dyrates")
    private String dyrates;
  }
}
