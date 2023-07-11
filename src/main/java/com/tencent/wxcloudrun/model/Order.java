package com.tencent.wxcloudrun.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Order  implements Serializable {
    private String id; //订单标识
    private String source; //来源： pdd/jd/tb
    private String orderId; // 订单号
    private String orderTime; // 下单时间,格式yyyy-MM-dd HH:mm:ss
    private String modifyTime; // 更新时间,格式yyyy-MM-dd HH:mm:ss
    private String finishTime; // 完成时间（购买用户确认收货时间）,格式yyyy-MM-dd HH:mm:ss
    private int orderEmt;//下单设备 1.pc 2.无线
    private int plus;//下单用户是否为PLUS会员 0：否，1：是
    private int compare;//比价状态：0：正常，1：比价
    private String unionId;//推客ID
    private String pid;//推广位ID
    private String skuId;//商品ID
    private String skuName;//商品名称
    private String skuNum;//商品数量
    private String imageUrl;//商品主图
    private String price;//商品单价
    private String commissionRate;//佣金比例(投放的广告主计划比例)
    private String finalRate;//最终分佣比例（单位：%）=分成比例+补贴比例
    private String estimateCosPrice;//预估计佣金额
    private String estimateFee;//推客的预估佣金
    private String actualCosPrice;//实际计算佣金的金额
    private String actualFee;//推客分得的实际佣金
    private String status;//订单状态
    private String uid;//订单状态
}
