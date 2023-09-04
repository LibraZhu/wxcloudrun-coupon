package com.tencent.wxcloudrun.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class HJKWPHOrder {

    private String orderSn;
    private Integer status;
    private Integer newCustomer;
    private String channelTag;
    private Long orderTime;
    private Long signTime;
    private Long settledTime;
    private List<DetailListDTO> detailList;
    private Long lastUpdateTime;
    private Integer settled;
    private Integer selfBuy;
    private String orderSubStatusName;
    private String commission;
    private String afterSaleChangeCommission;
    private Integer afterSaleChangeGoodsCount;
    private Long commissionEnterTime;
    private String orderSource;
    private String pid;
    private Integer isPrepay;
    private String statParam;
    private Integer isSplit;
    private String parentSn;
    private Integer orderTrackReason;
    private String appKey;

    @NoArgsConstructor
    @Data
    public static class DetailListDTO {
        private String goodsId;
        private String goodsName;
        private String goodsThumb;
        private Integer goodsCount;
        private String commissionTotalCost;
        private String commissionRate;
        private String commission;
        private String commCode;
        private String commName;
        private String orderSource;
        private Object afterSaleInfo;
        private String sizeId;
        private Integer status;
        private String brandStoreSn;
        private String brandStoreName;
    }
}
