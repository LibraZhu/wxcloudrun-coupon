package com.tencent.wxcloudrun.dto;

import lombok.Data;

@Data
public class HJKJDProduct {
  private Integer source; // 商品来源
  private String goods_id; // 商品id
  private String goods_name; // 商品名称
  private String goods_short_name; // 短标题
  private String goods_desc; // 导购信息
  private String goods_url; // 商品链接
  private String price; // 原价
  private String price_after; // 券后价
  private String discount; // 券金额
  private String picurl; // 图片地址
  private String picurls; // 多图
  private String couponurl; // 优惠券链接
  private Long sales; // 30天引单数量
  private String salesTip; // 已售卖件数
  private String owner; // g=自营，p=pop
  private Long comments; // 评论数
  private String is_jdwl; // 是否京东物流0否1是
  private Integer ispg; // 是否拼购 0否1是
  private String commission; // 预估佣金(券后)
  private String commissionshare; // 佣金比例
  private String plusCommissionShare; // plus佣金比例，plus用户购买推广者能获取到的佣金比例
  private String rebate; // 返利
  private String shopname; // 店铺名称
  private String searchId; //
  private String discountWph; // 唯品会折扣
  private String tkl; // 抖音口令
  private String click_url;
  private Boolean is_tmall;
}
