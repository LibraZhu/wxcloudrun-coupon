package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class HJKTBLink {
    @JsonProperty("category_id")
    private String categoryId;
    @JsonProperty("coupon_click_url")
    private String couponClickUrl;
    @JsonProperty("item_id")
    private String itemId;
    @JsonProperty("item_url")
    private String itemUrl;
    @JsonProperty("max_commission_rate")
    private String maxCommissionRate;
    @JsonProperty("reward_info")
    private String rewardInfo;
    @JsonProperty("cat_leaf_name")
    private String catLeafName;
    @JsonProperty("cat_name")
    private String catName;
    @JsonProperty("free_shipment")
    private String freeShipment;
    @JsonProperty("hot_flag")
    private String hotFlag;
    @JsonProperty("input_num_iid")
    private String inputNumIid;
    @JsonProperty("ju_online_end_time")
    private String juOnlineEndTime;
    @JsonProperty("ju_online_start_time")
    private String juOnlineStartTime;
    @JsonProperty("ju_pre_show_end_time")
    private String juPreShowEndTime;
    @JsonProperty("ju_pre_show_start_time")
    private String juPreShowStartTime;
    @JsonProperty("kuadian_promotion_info")
    private String kuadianPromotionInfo;
    @JsonProperty("material_lib_type")
    private String materialLibType;
    @JsonProperty("nick")
    private String nick;
    @JsonProperty("num_iid")
    private String numIid;
    @JsonProperty("pict_url")
    private String pictUrl;
    @JsonProperty("presale_deposit")
    private String presaleDeposit;
    @JsonProperty("presale_end_time")
    private String presaleEndTime;
    @JsonProperty("presale_start_time")
    private String presaleStartTime;
    @JsonProperty("presale_tail_end_time")
    private String presaleTailEndTime;
    @JsonProperty("presale_tail_start_time")
    private String presaleTailStartTime;
    @JsonProperty("provcity")
    private String provcity;
    @JsonProperty("reserve_price")
    private String reservePrice;
    @JsonProperty("seller_id")
    private String sellerId;
    @JsonProperty("small_images")
    private HJKTBLinkResponse.TbLink.SmallImagesDTO smallImages;
    @JsonProperty("superior_brand")
    private String superiorBrand;
    @JsonProperty("title")
    private String title;
    @JsonProperty("tmall_play_activity_end_time")
    private String tmallPlayActivityEndTime;
    @JsonProperty("tmall_play_activity_start_time")
    private String tmallPlayActivityStartTime;
    @JsonProperty("user_type")
    private String userType;
    @JsonProperty("volume")
    private String volume;
    @JsonProperty("zk_final_price")
    private String zkFinalPrice;
    @JsonProperty("commission_rate")
    private Integer commissionRate;
    @JsonProperty("coupon_short_url")
    private String couponShortUrl;
    @JsonProperty("ios_tbk_pwd")
    private String iosTbkPwd;
    @JsonProperty("tbk_pwd")
    private String tbkPwd;
    @JsonProperty("tkl")
    private String tkl;

    @NoArgsConstructor
    @Data
    public static class SmallImagesDTO {
        @JsonProperty("string")
        private List<String> string;
    }
}
