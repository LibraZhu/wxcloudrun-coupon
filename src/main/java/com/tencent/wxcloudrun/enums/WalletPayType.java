package com.tencent.wxcloudrun.enums;

/** 交易方式 */
public enum WalletPayType {
  NONE(0, "待定"),
  WX(1, "微信"),
  BANK(2, "银行卡"),
  SETTLE(3, "结算");
  private final int code;
  private final String message;

  WalletPayType(int code, String message) {
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
