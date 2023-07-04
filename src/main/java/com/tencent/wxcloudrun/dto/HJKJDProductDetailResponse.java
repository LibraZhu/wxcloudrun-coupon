package com.tencent.wxcloudrun.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class HJKJDProductDetailResponse extends HJKResponse {
    private HJKJDProductDetail data;

    @Data
    public static class HJKJDProductDetail {
        private String goods_id;
        private String goods_name;
        private String goods_short_name;
        private String price;
        private String price_after;
        private String discount;
        private String picurl;
        private String couponurl;
        private String commission;
        private String commissionshare;
        private String plusCommissionShare;
    }
}
