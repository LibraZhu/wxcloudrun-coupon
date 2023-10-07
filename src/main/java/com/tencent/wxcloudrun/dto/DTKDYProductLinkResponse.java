package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DTKDYProductLinkResponse {

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
        @JsonProperty("dyDeeplink")
        private String dyDeeplink;
        @JsonProperty("dyPassword")
        private String dyPassword;
        @JsonProperty("qrCode")
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
