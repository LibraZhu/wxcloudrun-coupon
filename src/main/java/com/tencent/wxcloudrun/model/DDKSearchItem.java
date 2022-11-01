package com.tencent.wxcloudrun.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class DDKSearchItem {
    private List<Integer> activityTags;
    private String brandName;
    private List<Integer> catIds;
    private Integer couponDiscount;
    private Integer couponEndTime;
    private Integer couponMinOrderAmount;
    private Integer couponRemainQuantity;
    private Integer couponStartTime;
    private Integer couponTotalQuantity;
    private String descTxt;
    private Integer extraCouponAmount;
    private String goodsDesc;
    private String goodsImageUrl;
    private String goodsName;
    private String goodsSign;
    private String goodsThumbnailUrl;
    private Boolean hasCoupon;
    private Boolean hasMallCoupon;
    private Boolean hasMaterial;
    private String lgstTxt;
    private Integer mallCouponDiscountPct;
    private Integer mallCouponEndTime;
    private Integer mallCouponId;
    private Integer mallCouponMaxDiscountAmount;
    private Integer mallCouponMinOrderAmount;
    private Integer mallCouponRemainQuantity;
    private Integer mallCouponStartTime;
    private Integer mallCouponTotalQuantity;
    private Integer mallCps;
    private Integer mallId;
    private String mallName;
    private Integer merchantType;
    private Integer minGroupPrice;
    private Integer minNormalPrice;
    private Boolean onlySceneAuth;
    private Integer optId;
    private List<Integer> optIds;
    private String optName;
    private Integer planType;
    private Integer predictPromotionRate;
    private Integer promotionRate;
    private String salesTip;
    private String searchId;
    private String servTxt;
    private List<Integer> serviceTags;
    private Integer shareRate;
    private Integer subsidyAmount;
    private List<String> unifiedTags;
    private Integer zsDuoId;
}
