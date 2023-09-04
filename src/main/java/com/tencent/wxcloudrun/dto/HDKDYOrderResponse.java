package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class HDKDYOrderResponse {

  private Integer code;
  private String msg;
  private List<DataDTO> data;
  private Integer minId;

  @NoArgsConstructor
  @Data
  public static class DataDTO {
    @JsonProperty("earning_time")
    private String earningTime;
    @JsonProperty("order_status")
    private String orderStatus;
    @JsonProperty("shop_name")
    private String shopName;
    @JsonProperty("item_title")
    private String itemTitle;
    @JsonProperty("item_img")
    private String itemImg;
    @JsonProperty("paid_time")
    private String paidTime;
    @JsonProperty("trade_id")
    private String tradeId;
    @JsonProperty("trade_parent_id")
    private String tradeParentId;
    @JsonProperty("refund_time")
    private String refundTime;
    @JsonProperty("create_time")
    private String createTime;
    @JsonProperty("settled_status")
    private String settledStatus;
    @JsonProperty("pay_price")
    private String payPrice;
    @JsonProperty("predict_money")
    private String predictMoney;
    @JsonProperty("actual_money")
    private String actualMoney;
    @JsonProperty("shop_id")
    private String shopId;
    @JsonProperty("channel_code")
    private String channelCode;
    @JsonProperty("update_time")
    private String updateTime;
    @JsonProperty("item_num")
    private String itemNum;
    @JsonProperty("product_id")
    private String productId;
    @JsonProperty("is_receipt")
    private String isReceipt;
  }
}
