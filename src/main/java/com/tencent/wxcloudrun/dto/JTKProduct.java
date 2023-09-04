package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class JTKProduct {
  @JsonProperty("goodsId")
  private String goodsId;

  @JsonProperty("goodsName")
  private String goodsName;

  @JsonProperty("goodsThumbUrl")
  private String goodsThumbUrl;

  @JsonProperty("price")
  private String price;

  @JsonProperty("marketPrice")
  private String marketPrice;

  @JsonProperty("sales_tip")
  private Integer salesTip;

  @JsonProperty("discount")
  private Integer discount;

  @JsonProperty("commissionRate")
  private String commissionRate;

  @JsonProperty("commission")
  private String commission;

  @JsonProperty("couponInfo")
  private CouponInfoDTO couponInfo;

  @JsonProperty("we_app_info")
  private WeAppInfoDTO weAppInfo;

  @JsonProperty("tkl")
  private String tkl;

  @JsonProperty("url")
  private String url;

  @JsonProperty("click_url")
  private String clickUrl;

  @JsonProperty("goodsCarouselPictures")
  private List<String> goodsCarouselPictures;

  @JsonProperty("goodsDetailPictures")
  private List<String> goodsDetailPictures;

  @JsonProperty("shopName")
  private String shopName;

  @NoArgsConstructor
  @Data
  public static class CouponInfoDTO {
    @JsonProperty("fav")
    private Integer fav;

    @JsonProperty("useEndTime")
    private Integer useEndTime;

    @JsonProperty("useBeginTime")
    private Integer useBeginTime;
  }

  @NoArgsConstructor
  @Data
  public static class WeAppInfoDTO {
    @JsonProperty("app_id")
    private String appId;

    @JsonProperty("page_path")
    private String pagePath;
  }
}
