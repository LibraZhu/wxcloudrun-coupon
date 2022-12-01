package com.tencent.wxcloudrun.utils;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

public class WxMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {
    public WxMappingJackson2HttpMessageConverter() {
        List<MediaType> mediaTypeList = new ArrayList<>();
        mediaTypeList.add(MediaType.TEXT_PLAIN);
        mediaTypeList.add(MediaType.TEXT_HTML);
        setSupportedMediaTypes(mediaTypeList);
    }
}
