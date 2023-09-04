package com.tencent.wxcloudrun.enums;

/** 交易方式 */
public enum WalletPayType {
  NONE(0, "待定"),
  BANK(1, "银行卡"),
  WX(2, "微信"),
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
