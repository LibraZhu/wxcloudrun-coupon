package com.tencent.wxcloudrun.config.properties;

import lombok.Data;

@Data
public class TBProperties {
    private String url;
    private String appKey;
    private String appSecret;
    private Long pid;
    private String xpid;
    private String rate;

}
