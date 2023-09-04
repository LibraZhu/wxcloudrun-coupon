package com.tencent.wxcloudrun.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderDto {
  private Long id;

  /** 来源: 1->pdd; 2->jd; 3->tb; 4->dy; 5->wph */
  private Integer orderSource;

  /** 订单号 */
  private String orderId;

  /** 订单标识 */
  private String orderSn;

  /** 下单时间 */
  private LocalDateTime orderTime;

  /** 更新时间 */
  private LocalDateTime modifyTime;

  /** 完成时间（购买用户确认收货时间） */
  private LocalDateTime finishTime;

  /** 预估结算时间 */
  private String settlementTime;

  /** 下单设备 1->pc; 2->无线 */
  private Integer orderEmt;

  /** 是否为PLUS会员 0->否; 1->是 */
  private Integer plus;

  /** 比价状态：0->正常; 1->比价 */
  private Integer compared;

  /** 商品ID */
  private String skuId;

  /** 商品名称 */
  private String skuName;

  /** 商品数量 */
  private Long skuNum;

  /** 主图 */
  private String imageUrl;

  /** 商品单价 */
  private String price;

  /** 商品单价 */
  private String priceTotal;

  /** 返利 */
  private String rebate;

  /** 订单状态 1->待收货；2->待结算；3->已结算；4->审核失败 */
  private Integer status;

  /** 用户标识 */
  private String uid;

  /** 订单状态描述 */
  private String statusDes;
}
