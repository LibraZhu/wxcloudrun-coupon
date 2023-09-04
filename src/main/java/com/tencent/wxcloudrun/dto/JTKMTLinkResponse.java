package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class JTKMTLinkResponse {

  @JsonProperty("code")
  private Integer code;
  @JsonProperty("msg")
  private String msg;
  @JsonProperty("data")
  private DataDTO data;

  @NoArgsConstructor
  @Data
  public static class DataDTO {
    @JsonProperty("h5")
    private String h5;
    @JsonProperty("short_h5")
    private String shortH5;
    @JsonProperty("deeplink")
    private String deeplink;
    @JsonProperty("h5_evoke")
    private String h5Evoke;
    @JsonProperty("tkl")
    private String tkl;
    @JsonProperty("we_app_info")
    private WeAppInfoDTO weAppInfo;

    @NoArgsConstructor
    @Data
    public static class WeAppInfoDTO {
      @JsonProperty("app_id")
      private String appId;
      @JsonProperty("page_path")
      private String pagePath;
      @JsonProperty("miniCode")
      private String miniCode;
    }
  }
}
