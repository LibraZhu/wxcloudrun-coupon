package com.tencent.wxcloudrun.enums;

/** 订单来源 */
public enum ProductSource {
  PDD(1, "拼多多"),
  JD(2, "京东"),
  TB(3, "淘宝"),
  DY(4, "抖音"),
  WPH(5, "唯品会");
  private final int code;
  private final String message;

  ProductSource(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
