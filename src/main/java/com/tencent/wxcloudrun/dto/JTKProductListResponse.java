package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class JTKProductListResponse {

  @JsonProperty("code")
  private Integer code;

  @JsonProperty("msg")
  private String msg;

  @JsonProperty("data")
  private List<JTKProduct> data;

  @JsonProperty("total_results")
  private Long totalResults;
}
