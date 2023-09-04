package com.tencent.wxcloudrun.enums;

/** 交易类型 */
public enum WalletPayStatus {
  NO(0, "待支付"),
  SUCCESS(1, "支付成功"),
  FAIL(2, "支付失败");
  private final int code;
  private final String message;

  WalletPayStatus(int code, String message) {
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
