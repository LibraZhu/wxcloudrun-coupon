package com.tencent.wxcloudrun.model;

import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class WxMessage {
    @Alias("ToUserName")
    private String toUserName;
    @Alias("FromUserName")
    private String fromUserName;
    @Alias("CreateTime")
    private String createTime;
    @Alias("MsgType")
    private String msgType;
    @Alias("ArticleCount")
    private Integer articleCount;
    @Alias("Articles")
    private List<Articles> articles;
    @Alias("Content")
    private String content;

    @Data
    public static class Articles {
        @Alias("Title")
        private String title;
        @Alias("Description")
        private String description;
        @Alias("PicUrl")
        private String picUrl;
        @Alias("Url")
        private String url;
    }
}
