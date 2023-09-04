package com.tencent.wxcloudrun.config;

import com.tencent.wxcloudrun.config.properties.*;
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
  public TBProperties taobaoProperties() {
    return new TBProperties();
  }

  @Bean
  @ConfigurationProperties(prefix = "jd")
  public JDProperties jdProperties() {
    return new JDProperties();
  }

  @Bean
  @ConfigurationProperties(prefix = "wph")
  public WPHProperties wphProperties() {
    return new WPHProperties();
  }

  @Bean
  @ConfigurationProperties(prefix = "dy")
  public DYProperties dyProperties() {
    return new DYProperties();
  }

  @Bean
  @ConfigurationProperties(prefix = "hjk")
  public HJKProperties hjkProperties() {
    return new HJKProperties();
  }

  @Bean
  @ConfigurationProperties(prefix = "hdk")
  public HDKProperties hdkProperties() {
    return new HDKProperties();
  }

  @Bean
  @ConfigurationProperties(prefix = "jtk")
  public JTKProperties jtkProperties() {
    return new JTKProperties();
  }
}
