package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WxMessageRequest {
    @JsonProperty("ToUserName")
    private String toUserName;
    @JsonProperty("FromUserName")
    private String fromUserName;
    @JsonProperty("CreateTime")
    private String createTime;
    @JsonProperty("MsgType")
    private String msgType;
    @JsonProperty("Content")
    private String content;
    @JsonProperty("MsgId")
    private String msgId;
}
