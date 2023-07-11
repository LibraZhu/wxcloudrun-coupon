package com.tencent.wxcloudrun.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
public class HDKJDProductDetailResponse {
    private int code;
    private String msg;
    private List<HDKJDProductDetail> data;

    @Data
    public static class HDKJDProductDetail {
        private String skuid;
        private String goodsname;
        private String itemprice;
        private String itemendprice;
        private String itempic;
        private String couponurl;
        private String couponmoney;
        private String commission;
        private String commissionshare;
    }
}
