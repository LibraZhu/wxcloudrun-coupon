package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class JTKLinkResponse {

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
        @JsonProperty("we_app_info")
        private WeAppInfoDTO weAppInfo;
        @JsonProperty("act_name")
        private String actName;

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
