package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class HJKTBOrderResponse extends HJKResponse {
  private Object data;

  @Data
  public static class OrderPage {
    private String has_next;
    private String has_pre;
    private String page_no;
    private String page_size;
    private String position_index;
    private OrderResult results;
  }

  @Data
  public static class OrderResult {
    private List<OrderDto> publisher_order_dto;
  }

  @NoArgsConstructor
  @Data
  public static class OrderDto {

    @JsonProperty("tb_paid_time")
    private String tbPaidTime;
    @JsonProperty("tk_paid_time")
    private String tkPaidTime;
    @JsonProperty("pay_price")
    private String payPrice;
    @JsonProperty("pub_share_fee")
    private String pubShareFee;
    @JsonProperty("trade_id")
    private String tradeId;
    @JsonProperty("tk_order_role")
    private Integer tkOrderRole;
    @JsonProperty("tk_earning_time")
    private String tkEarningTime;
    @JsonProperty("adzone_id")
    private Long adzoneId;
    @JsonProperty("pub_share_rate")
    private String pubShareRate;
    @JsonProperty("unid")
    private String unid;
    @JsonProperty("refund_tag")
    private Integer refundTag;
    @JsonProperty("subsidy_rate")
    private String subsidyRate;
    @JsonProperty("tk_total_rate")
    private String tkTotalRate;
    @JsonProperty("item_category_name")
    private String itemCategoryName;
    @JsonProperty("seller_nick")
    private String sellerNick;
    @JsonProperty("pub_id")
    private Integer pubId;
    @JsonProperty("alimama_rate")
    private String alimamaRate;
    @JsonProperty("subsidy_type")
    private String subsidyType;
    @JsonProperty("item_img")
    private String itemImg;
    @JsonProperty("pub_share_pre_fee")
    private String pubSharePreFee;
    @JsonProperty("alipay_total_price")
    private String alipayTotalPrice;
    @JsonProperty("item_title")
    private String itemTitle;
    @JsonProperty("site_name")
    private String siteName;
    @JsonProperty("item_num")
    private Integer itemNum;
    @JsonProperty("subsidy_fee")
    private String subsidyFee;
    @JsonProperty("alimama_share_fee")
    private String alimamaShareFee;
    @JsonProperty("trade_parent_id")
    private String tradeParentId;
    @JsonProperty("order_type")
    private String orderType;
    @JsonProperty("tk_create_time")
    private String tkCreateTime;
    @JsonProperty("flow_source")
    private String flowSource;
    @JsonProperty("terminal_type")
    private String terminalType;
    @JsonProperty("click_time")
    private String clickTime;
    @JsonProperty("tk_status")
    private Integer tkStatus;
    @JsonProperty("item_price")
    private String itemPrice;
    @JsonProperty("item_id")
    private String itemId;
    @JsonProperty("adzone_name")
    private String adzoneName;
    @JsonProperty("total_commission_rate")
    private String totalCommissionRate;
    @JsonProperty("item_link")
    private String itemLink;
    @JsonProperty("site_id")
    private Integer siteId;
    @JsonProperty("seller_shop_title")
    private String sellerShopTitle;
    @JsonProperty("income_rate")
    private String incomeRate;
    @JsonProperty("total_commission_fee")
    private String totalCommissionFee;
    @JsonProperty("special_id")
    private Integer specialId;
    @JsonProperty("relation_id")
    private Integer relationId;
    @JsonProperty("deposit_price")
    private String depositPrice;
    @JsonProperty("tb_deposit_time")
    private String tbDepositTime;
    @JsonProperty("tk_deposit_time")
    private String tkDepositTime;
    @JsonProperty("tp_order_id")
    private String tpOrderId;
    @JsonProperty("marketing_type")
    private String marketingType;
    @JsonProperty("modified_time")
    private String modifiedTime;
    @JsonProperty("talent_pid")
    private String talentPid;
    @JsonProperty("tb_gmv_total_price")
    private String tbGmvTotalPrice;
    @JsonProperty("extra_mkt_id")
    private String extraMktId;
    @JsonProperty("untts")
    private String untts;
    @JsonProperty("pub_share_pre_fee_for_commission")
    private String pubSharePreFeeForCommission;
    @JsonProperty("pub_share_fee_for_commission")
    private String pubShareFeeForCommission;
    @JsonProperty("pub_share_rate_for_sdy")
    private String pubShareRateForSdy;
    @JsonProperty("tk_total_rate_for_sdy")
    private String tkTotalRateForSdy;
    @JsonProperty("pub_share_pre_fee_for_sdy")
    private String pubSharePreFeeForSdy;
    @JsonProperty("pub_share_fee_for_sdy")
    private String pubShareFeeForSdy;
    @JsonProperty("platform_special_service_rate")
    private String platformSpecialServiceRate;
    @JsonProperty("platform_special_service_fee")
    private String platformSpecialServiceFee;
  }
}
