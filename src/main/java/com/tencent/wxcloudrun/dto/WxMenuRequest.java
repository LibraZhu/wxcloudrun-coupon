package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class WxMenuRequest {
    private List<Button> button;

    @Data
    public static class  Button {
        private String type;
        private String name;
        private String key;
        private String url;
        private String appid;
        private String pagepath;
        @JsonProperty("sub_button")
        private List<Button> subButton;
    }
}
