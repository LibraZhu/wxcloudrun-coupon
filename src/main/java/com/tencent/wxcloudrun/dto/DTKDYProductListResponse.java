package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class DTKDYProductListResponse {
  @JsonProperty("cache")
  private Boolean cache;

  @JsonProperty("code")
  private Integer code;

  @JsonProperty("data")
  private DataDTO data;

  @JsonProperty("msg")
  private String msg;

  @JsonProperty("time")
  private Long time;

  @NoArgsConstructor
  @Data
  public static class DataDTO {
    @JsonProperty("list")
    private List<DTKDYProduct> list;

    @JsonProperty("total")
    private Long total;
  }
}
