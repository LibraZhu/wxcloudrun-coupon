package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class JTKDYOrderResponse {

  @JsonProperty("code")
  private Integer code;

  @JsonProperty("msg")
  private String msg;

  @JsonProperty("data")
  private Object data;

  @NoArgsConstructor
  @Data
  public static class DataDTO {
    @JsonProperty("total")
    private Integer total;

    @JsonProperty("per_page")
    private Integer perPage;

    @JsonProperty("current_page")
    private Integer currentPage;

    @JsonProperty("last_page")
    private Integer lastPage;

    @JsonProperty("data")
    private List<DataDTO.OrderDTO> data;

    @NoArgsConstructor
    @Data
    public static class OrderDTO {
      @JsonProperty("order_id")
      private String orderId;

      @JsonProperty("sid")
      private String sid;

      @JsonProperty("jtk_share_rate")
      private String jtkShareRate;

      @JsonProperty("jtk_share_fee")
      private String jtkShareFee;

      @JsonProperty("pay_date")
      private String payDate;

      @JsonProperty("pay_success_time")
      private String paySuccessTime;

      @JsonProperty("update_time")
      private String updateTime;

      @JsonProperty("settle_time")
      private String settleTime;


      @JsonProperty("product_id")
      private String productId;

      @JsonProperty("product_name")
      private String productName;

      @JsonProperty("product_img")
      private String productImg;

      @JsonProperty("shop_name")
      private String shopName;

      @JsonProperty("total_pay_amount")
      private String totalPayAmount;

      @JsonProperty("flow_point")
      private String flowPoint;

      @JsonProperty("item_num")
      private Integer itemNum;
    }
  }
}
