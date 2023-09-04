package com.tencent.wxcloudrun.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HJKProductListResponse extends HJKResponse {
  private HJKProductList data;

  @Data
  public static class HJKProductList {
    private Object data;
    private Object goodsInfoList;
    private long total;
  }
}
