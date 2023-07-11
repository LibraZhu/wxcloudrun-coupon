package com.tencent.wxcloudrun.config.properties;

import lombok.Data;

@Data
public class ClientProperties {

    // 多多客
    private String clientId;
    private String clientSecret;
    private String pid;
    private String uid;
    private String rate;

}
