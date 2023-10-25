package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class TBProductListResponse {

  @JsonProperty("error")
  private String error;
  @JsonProperty("msg")
  private String msg;
  @JsonProperty("search_type")
  private Integer searchType;
  @JsonProperty("is_similar")
  private String isSimilar;
  @JsonProperty("is_splitWord")
  private Integer isSplitword;
  @JsonProperty("force_index")
  private String forceIndex;
  @JsonProperty("total_results")
  private Integer totalResults;
  @JsonProperty("result_list")
  private List<ResultListDTO> resultList;
  @JsonProperty("request_id")
  private String requestId;

  @NoArgsConstructor
  @Data
  public static class ResultListDTO {
    @JsonProperty("category_id")
    private Integer categoryId;
    @JsonProperty("category_name")
    private String categoryName;
    @JsonProperty("commission_rate")
    private String commissionRate;
    @JsonProperty("coupon_amount")
    private String couponAmount;
    @JsonProperty("coupon_end_time")
    private String couponEndTime;
    @JsonProperty("coupon_id")
    private String couponId;
    @JsonProperty("coupon_info")
    private String couponInfo;
    @JsonProperty("coupon_remain_count")
    private Integer couponRemainCount;
    @JsonProperty("coupon_start_fee")
    private String couponStartFee;
    @JsonProperty("coupon_start_time")
    private String couponStartTime;
    @JsonProperty("coupon_total_count")
    private Integer couponTotalCount;
    @JsonProperty("include_dxjh")
    private String includeDxjh;
    @JsonProperty("info_dxjh")
    private String infoDxjh;
    @JsonProperty("item_description")
    private String itemDescription;
    @JsonProperty("item_id")
    private String itemId;
    @JsonProperty("item_url")
    private String itemUrl;
    @JsonProperty("level_one_category_id")
    private Integer levelOneCategoryId;
    @JsonProperty("level_one_category_name")
    private String levelOneCategoryName;
    @JsonProperty("nick")
    private String nick;
    @JsonProperty("num_iid")
    private String numIid;
    @JsonProperty("pict_url")
    private String pictUrl;
    @JsonProperty("presale_deposit")
    private String presaleDeposit;
    @JsonProperty("provcity")
    private String provcity;
    @JsonProperty("real_post_fee")
    private String realPostFee;
    @JsonProperty("reserve_price")
    private String reservePrice;
    @JsonProperty("seller_id")
    private String sellerId;
    @JsonProperty("shop_dsr")
    private Integer shopDsr;
    @JsonProperty("shop_title")
    private String shopTitle;
    @JsonProperty("short_title")
    private String shortTitle;
    @JsonProperty("small_images")
    private List<String> smallImages;
    @JsonProperty("superior_brand")
    private String superiorBrand;
    @JsonProperty("title")
    private String title;
    @JsonProperty("tk_total_commi")
    private String tkTotalCommi;
    @JsonProperty("tk_total_sales")
    private String tkTotalSales;
    @JsonProperty("user_type")
    private Integer userType;
    @JsonProperty("volume")
    private Integer volume;
    @JsonProperty("white_image")
    private String whiteImage;
    @JsonProperty("zk_final_price")
    private String zkFinalPrice;
  }
}
