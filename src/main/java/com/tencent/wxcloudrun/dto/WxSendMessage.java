package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class WxSendMessage {
    @JsonProperty("touser")
    private String toUser;
    @JsonProperty("msgtype")
    private String msgType;
    @JsonProperty("link")
    private Link link;

    @Data
    public static class Link {
        @JsonProperty("title")
        private String title;
        @JsonProperty("description")
        private String description;
        @JsonProperty("thumb_url")
        private String thumbUrl;
        @JsonProperty("url")
        private String url;
    }
}
