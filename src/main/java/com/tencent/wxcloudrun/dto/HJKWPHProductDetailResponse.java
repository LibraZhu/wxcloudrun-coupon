package com.tencent.wxcloudrun.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class HJKWPHProductDetailResponse extends HJKResponse {
  private List<HJKWPHProduct> data;
}
