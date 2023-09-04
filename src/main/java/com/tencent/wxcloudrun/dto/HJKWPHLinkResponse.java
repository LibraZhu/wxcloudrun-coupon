package com.tencent.wxcloudrun.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class HJKWPHLinkResponse extends HJKResponse {
  private WPHLink data;

  @Data
  public static class WPHLink {
    private List<WPHLinkUrlInfo> urlInfoList;
  }
  @Data
  public static class WPHLinkUrlInfo {
    private String url;
    private String longUrl;
    private String ulUrl;
    private String deeplinkUrl;
    private String vipWxUrl;
    private String vipWxCode;
  }
}
