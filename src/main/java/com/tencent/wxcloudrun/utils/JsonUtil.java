package com.tencent.wxcloudrun.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
  static ObjectMapper objectMapper = new ObjectMapper();

  static {
    // 反序列化：JSON字段中有Java对象中没有的字段时不报错
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    // 序列化：序列化BigDecimal时不使用科学计数法输出
    objectMapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
  }
  /**
   * 对象转字符串
   *
   * @param obj
   * @return
   */
  public static String toJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return "";
  }

  /**
   * 字符串转对象
   *
   * @param str
   * @param clazz
   * @param <T>
   * @return
   */
  public static <T> T toObj(String str, Class<T> clazz) {
    try {
      return objectMapper.readValue(str, clazz);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 字符串转列表
   *
   * @param str
   * @param valueTypeRef
   * @param <T>
   * @return
   */
  public static <T> T toList(String str, TypeReference<T> valueTypeRef) {
    try {
      return objectMapper.readValue(str, valueTypeRef);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return null;
  }
}
