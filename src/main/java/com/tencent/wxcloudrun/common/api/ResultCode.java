package com.tencent.wxcloudrun.common.api;

/**
 * 常用API返回对象
 */
public enum ResultCode implements IErrorCode {
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    NO_PRODUCT(501, "没有找到商品"),
    NO_TB_AUTH(502, "淘宝未授权"),
    VALIDATE_FAILED(401, "参数检验失败"),
    UNAUTHORIZED(402, "暂未登录或token已经过期"),
    FORBIDDEN(403, "没有相关权限");
    private long code;
    private String message;

    private ResultCode(long code, String message) {
        this.code = code;
        this.message = message;
    }

    public long getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
