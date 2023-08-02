package com.tencent.wxcloudrun.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class HJKJDProductListResponse extends HJKResponse {
  private HJKJDProductList data;

  @Data
  public static class HJKJDProductList {
    private List<HJKJDProduct> data;
    private long total;
  }
}
