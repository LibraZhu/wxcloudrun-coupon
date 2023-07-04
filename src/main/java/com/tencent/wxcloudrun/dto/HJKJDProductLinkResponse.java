package com.tencent.wxcloudrun.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class HJKJDProductLinkResponse extends HJKResponse {
    private String data;
}
