package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class DTKDYOrderResponse {

  @JsonProperty("cache")
  private Boolean cache;
  @JsonProperty("code")
  private Integer code;
  @JsonProperty("data")
  private DataDTO data;
  @JsonProperty("msg")
  private String msg;
  @JsonProperty("requestId")
  private String requestId;
  @JsonProperty("time")
  private Long time;

  @NoArgsConstructor
  @Data
  public static class DataDTO {
    @JsonProperty("list")
    private List<ListDTO> list;
    @JsonProperty("page")
    private Integer page;
    @JsonProperty("total")
    private Integer total;

    @NoArgsConstructor
    @Data
    public static class ListDTO {
      @JsonProperty("ads_estimated_commission")
      private Double adsEstimatedCommission;
      @JsonProperty("ads_real_commission")
      private Double adsRealCommission;
      @JsonProperty("commission_rate")
      private Double commissionRate;
      @JsonProperty("estimated_total_commission")
      private Double estimatedTotalCommission;
      @JsonProperty("external_info")
      private String externalInfo;
      @JsonProperty("flow_point")
      private String flowPoint;
      @JsonProperty("item_num")
      private Integer itemNum;
      @JsonProperty("media_type_name")
      private String mediaTypeName;
      @JsonProperty("order_id")
      private String orderId;
      @JsonProperty("pay_goods_amount")
      private Double payGoodsAmount;
      @JsonProperty("pay_success_time")
      private String paySuccessTime;
      @JsonProperty("product_id")
      private String productId;
      @JsonProperty("product_img")
      private String productImg;
      @JsonProperty("product_name")
      private String productName;
      @JsonProperty("real_commission")
      private Double realCommission;
      @JsonProperty("refund_time")
      private String refundTime;
      @JsonProperty("settle_time")
      private String settleTime;
      @JsonProperty("settled_goods_amount")
      private Double settledGoodsAmount;
      @JsonProperty("settled_tech_service_fee")
      private Double settledTechServiceFee;
      @JsonProperty("shop_name")
      private String shopName;
      @JsonProperty("total_pay_amount")
      private Double totalPayAmount;
      @JsonProperty("update_time")
      private String updateTime;
      @JsonProperty("order_type")
      private Integer orderType;
    }
  }
}
