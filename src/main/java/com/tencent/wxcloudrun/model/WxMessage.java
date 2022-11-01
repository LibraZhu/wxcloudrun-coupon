package com.tencent.wxcloudrun.model;

import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class WxMessage {
    @JsonProperty("ToUserName")
    private String toUserName;
    @JsonProperty("FromUserName")
    private String fromUserName;
    @JsonProperty("CreateTime")
    private String createTime;
    @JsonProperty("MsgType")
    private String msgType;
    @JsonProperty("ArticleCount")
    private Integer articleCount;
    @JsonProperty("Articles")
    private List<Articles> articles;
    @JsonProperty("Content")
    private String content;

    @Data
    public static class Articles {
        @JsonProperty("Title")
        private String title;
        @JsonProperty("Description")
        private String description;
        @JsonProperty("PicUrl")
        private String picUrl;
        @JsonProperty("Url")
        private String url;
    }
}
