package com.tencent.wxcloudrun.config;

import com.tencent.wxcloudrun.config.properties.ClientProperties;
import com.tencent.wxcloudrun.config.properties.JDProperties;
import com.tencent.wxcloudrun.config.properties.TaobaoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfig {
    @Bean
    @ConfigurationProperties(prefix = "client")
    public ClientProperties clientProperties() {
        return new ClientProperties();
    }
    @Bean
    @ConfigurationProperties(prefix = "taobao")
    public TaobaoProperties taobaoProperties() {
        return new TaobaoProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "jd")
    public JDProperties jdProperties() {
        return new JDProperties();
    }

}
