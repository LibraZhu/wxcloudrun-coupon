package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class JTKProductDetailResponse {

  @JsonProperty("code")
  private Integer code;

  @JsonProperty("msg")
  private String msg;

  @JsonProperty("data")
  private JTKProduct data;
}
