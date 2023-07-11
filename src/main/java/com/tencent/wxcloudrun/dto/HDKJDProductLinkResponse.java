package com.tencent.wxcloudrun.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class HDKJDProductLinkResponse {
    private int code;
    private String msg;
    private HDKJDProductLinkDetail data;

    @Data
    public static class HDKJDProductLinkDetail {
        private String short_url;
    }
}
