package com.tencent.wxcloudrun.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HJKTBOrderResponse extends HJKResponse {
  private OrderPage data;

  @Data
  public static class OrderPage {
    private String has_next;
    private String has_pre;
    private String page_no;
    private String page_size;
    private String position_index;
    private OrderResult results;
  }

  @Data
  public static class OrderResult {
    private Object publisher_order_dto;
  }
}
