package com.tencent.wxcloudrun.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class HJKWPHProduct {
  private String goodsId;
  private String goodsName;
  private String goodsDesc;
  private String destUrl;
  private String goodsThumbUrl;
  private List<String> goodsCarouselPictures;
  private String goodsMainPicture;
  private Integer categoryId;
  private String categoryName;
  private Integer sourceType;
  private String marketPrice;
  private String vipPrice;
  private String commissionRate;
  private String commission;
  private List<String> goodsDetailPictures;
  private Integer cat1stId;
  private String cat1stName;
  private Integer cat2ndId;
  private String cat2ndName;
  private String brandStoreSn;
  private String brandName;
  private String brandLogoFull;
  private Long schemeEndTime;
  private Long sellTimeFrom;
  private Long sellTimeTo;
  private Integer weight;
  private StoreInfoDTO storeInfo;
  private CommentsInfoDTO commentsInfo;
  private StoreServiceCapabilityDTO storeServiceCapability;
  private Integer brandId;
  private Long schemeStartTime;
  private Integer saleStockStatus;
  private Integer status;
  private PrepayInfoDTO prepayInfo;
  private List<JoinedActivitiesDTO> joinedActivities;
  private CouponInfoDTO couponInfo;
  private Integer haiTao;
  private String spuId;
  private Object goodsIdsWithSameSpu;
  private Object skuInfos;
  private Object exclusiveCoupon;
  private Object cpsInfo;
  private String sn;
  private Object tagNames;
  private Object whiteImage;
  private Object futurePriceMsg;
  private Boolean isSubsidyActivityGoods;
  private Object subsidyActivityAmount;
  private Object subsidyTaskNo;
  private Integer couponPriceType;
  private String estimatePrice;
  private String goodsSoldNumDesc;
  private String productSales;
  private String destUrlPc;
  private String adCode;

  @NoArgsConstructor
  @Data
  public static class StoreInfoDTO {
    private String storeId;
    private String storeName;
  }

  @NoArgsConstructor
  @Data
  public static class CommentsInfoDTO {
    private Integer commentsX;
    private String goodCommentsShare;
  }

  @NoArgsConstructor
  @Data
  public static class StoreServiceCapabilityDTO {
    private String storeScore;
    private String storeRankRate;
  }

  @NoArgsConstructor
  @Data
  public static class PrepayInfoDTO {
    private Integer isPrepay;
    private Object prepayPrice;
    private Object firstAmount;
    private Object lastAmount;
    private Object prepayFavAmount;
    private Object deductionPrice;
    private Object prepayDiscount;
    private Object prepayFirstStartTime;
    private Object prepayFirstEndTime;
    private Object prepayLastStartTime;
    private Object prepayLastEndTime;
  }

  @NoArgsConstructor
  @Data
  public static class CouponInfoDTO {
    private String couponNo;
    private String couponName;
    private String buy;
    private String fav;
    private Long activateBeginTime;
    private Long activateEndTime;
    private Long useBeginTime;
    private Long useEndTime;
    private Integer totalAmount;
    private Integer activedAmount;
    private Integer couponType;
    private Object vipPrice;
    private Object hiddenCouponReceiveUrl;
    private Integer limit;
    private Object left;
  }

  @NoArgsConstructor
  @Data
  public static class JoinedActivitiesDTO {
    private Integer actType;
    private String actName;
    private Long beginTime;
    private Long endTime;
    private Long foreShowBeginTime;
  }
}
