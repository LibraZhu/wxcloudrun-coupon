package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class HDKDYLinkResponse {

  @JsonProperty("code")
  private Integer code;
  @JsonProperty("msg")
  private String msg;
  @JsonProperty("data")
  private DataDTO data;

  @NoArgsConstructor
  @Data
  public static class DataDTO {
    @JsonProperty("dy_deeplink")
    private String dyDeeplink;
    @JsonProperty("dy_password")
    private String dyPassword;
    @JsonProperty("qr_code")
    private QrCodeDTO qrCode;

    @NoArgsConstructor
    @Data
    public static class QrCodeDTO {
      @JsonProperty("height")
      private Integer height;
      @JsonProperty("url")
      private String url;
      @JsonProperty("width")
      private Integer width;
    }
  }
}
