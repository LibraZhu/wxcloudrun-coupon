package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class DTKDYProduct {
    @JsonProperty("productId")
    @JsonAlias("id")
    private String productId;
    @JsonProperty("title")
    private String title;
    @JsonProperty("cover")
    private String cover;
    @JsonProperty("imgs")
    private List<String> imgs;
    @JsonProperty("price")
    private String price;
    @JsonProperty("newUserPrice")
    private String newUserPrice;//抖音支付新用户价
    @JsonProperty("couponPrice")
    private String couponPrice;
    @JsonProperty("sales")
    private Integer sales;
    @JsonProperty("sales30day")
    private Integer sales30day;
    @JsonProperty("sales2h")
    private Integer sales2h;
    @JsonProperty("detailUrl")
    private String detailUrl;
    @JsonProperty("cosFee")
    private Double cosFee;
    @JsonProperty("cosRatio")
    private Double cosRatio;
    @JsonProperty("kolCosFee")
    private Double kolCosFee;
    @JsonProperty("kolCosRatio")
    private Double kolCosRatio;

    @JsonProperty("firstCid")
    private Integer firstCid;
    @JsonProperty("inStock")
    private Boolean inStock;
    @JsonProperty("secondCid")
    private Integer secondCid;
    @JsonProperty("sharable")
    private Boolean sharable;
    @JsonProperty("shopId")
    private Integer shopId;
    @JsonProperty("shopName")
    private String shopName;
    @JsonProperty("thirdCid")
    private Integer thirdCid;
}
