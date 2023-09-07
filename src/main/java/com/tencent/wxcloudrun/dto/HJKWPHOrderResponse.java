package com.tencent.wxcloudrun.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HJKWPHOrderResponse extends HJKResponse {
  private OrderPage data;

  @Data
  public static class OrderPage {
    private long total;
    private Object orderInfoList;
  }
}
