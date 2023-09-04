package com.tencent.wxcloudrun.enums;

/** 交易类型 */
public enum WalletRecordType {
  CASH_OUT(1, "提现"),
  SETTLE(2, "结算");
  private final int code;
  private final String message;

  WalletRecordType(int code, String message) {
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
