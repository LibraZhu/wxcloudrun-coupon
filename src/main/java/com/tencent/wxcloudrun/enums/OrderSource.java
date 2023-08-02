package com.tencent.wxcloudrun.enums;

/**
 * 订单来源
 */
public enum OrderSource {
    PDD(1, "拼多多"),
    JD(2, "京东"),
    TB(3, "淘宝败");
    private final int code;
    private final String message;

    OrderSource(int code, String message) {
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
