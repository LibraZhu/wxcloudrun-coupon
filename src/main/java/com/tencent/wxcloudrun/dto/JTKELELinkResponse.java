package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class JTKELELinkResponse {

  @JsonProperty("code")
  private Integer code;
  @JsonProperty("msg")
  private String msg;
  @JsonProperty("data")
  private DataDTO data;

  @NoArgsConstructor
  @Data
  public static class DataDTO {
    @JsonProperty("click_url")
    private String clickUrl;
    @JsonProperty("short_click_url")
    private String shortClickUrl;
    @JsonProperty("wx_miniprogram_path")
    private String wxMiniprogramPath;
    @JsonProperty("wx_qrcode_url")
    private String wxQrcodeUrl;
    @JsonProperty("app_id")
    private String appId;
    @JsonProperty("alipay_mini_url")
    private String alipayMiniUrl;
    @JsonProperty("ele_scheme_url")
    private String eleSchemeUrl;
  }
}
