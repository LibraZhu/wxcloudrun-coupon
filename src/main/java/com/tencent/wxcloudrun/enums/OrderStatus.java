package com.tencent.wxcloudrun.enums;

/** 订单来源 */
public enum OrderStatus {
  DELIVER(1, "待收货"),
  COMPLETE(2, "已完成，待结算"),
  SETTLED(3, "已结算"),
  INVALID(4, "审核失败");
  private final int code;
  private final String message;

  OrderStatus(int code, String message) {
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
