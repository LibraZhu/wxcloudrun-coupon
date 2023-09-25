package com.tencent.wxcloudrun.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HJKJDProductDetailResponse extends HJKResponse {
  private Object data;
}
