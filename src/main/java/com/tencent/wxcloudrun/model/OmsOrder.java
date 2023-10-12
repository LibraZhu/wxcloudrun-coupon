package com.tencent.wxcloudrun.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 订单表
 * </p>
 *
 * @author zjf
 * @since 2023年08月04日
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("oms_order")
public class OmsOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 来源: 1->pdd; 2->jd; 3->tb; 4->dy; 5->wph
     */
    @TableField("order_source")
    private Integer orderSource;

    /**
     * 订单号
     */
    @TableField("order_id")
    private String orderId;

    /**
     * 订单标识
     */
    @TableField("order_sn")
    private String orderSn;

    /**
     * 下单时间
     */
    @TableField("order_time")
    private LocalDateTime orderTime;

    /**
     * 更新时间
     */
    @TableField("modify_time")
    private LocalDateTime modifyTime;

    /**
     * 完成时间（购买用户确认收货时间）
     */
    @TableField("finish_time")
    private LocalDateTime finishTime;

    /**
     * 结算时间
     */
    @TableField("settle_time")
    private LocalDateTime settleTime;

    /**
     * 下单设备 1->pc; 2->无线
     */
    @TableField("order_emt")
    private Integer orderEmt;

    /**
     * 是否为PLUS会员 0->否; 1->是
     */
    @TableField("plus")
    private Integer plus;

    /**
     * 比价状态：0->正常; 1->比价
     */
    @TableField("compared")
    private Integer compared;

    /**
     * 推客ID
     */
    @TableField("union_id")
    private String unionId;

    /**
     * 推广pid
     */
    @TableField("pid")
    private String pid;

    /**
     * 商品ID
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * 商品名称
     */
    @TableField("sku_name")
    private String skuName;

    /**
     * 商品数量
     */
    @TableField("sku_num")
    private Long skuNum;

    /**
     * 主图
     */
    @TableField("image_url")
    private String imageUrl;

    /**
     * 商品单价
     */
    @TableField("price")
    private String price;

    /**
     * 佣金比例
     */
    @TableField("commission_rate")
    private String commissionRate;

    /**
     * 最终分佣比例
     */
    @TableField("final_rate")
    private String finalRate;

    /**
     * 预估计佣金额
     */
    @TableField("estimate_cos_price")
    private String estimateCosPrice;

    /**
     * 推客的预估佣金
     */
    @TableField("estimate_fee")
    private String estimateFee;

    /**
     * 实际计算佣金的金额
     */
    @TableField("actual_cos_price")
    private String actualCosPrice;

    /**
     * 推客分得的实际佣金
     */
    @TableField("actual_fee")
    private String actualFee;

    /**
     * 订单状态 1->待收货；2->待结算；3->已结算；4->审核失败
     */
    @TableField("status")
    private Integer status;

    /**
     * 用户标识
     */
    @TableField("uid")
    private String uid;

    /**
     * 订单状态描述
     */
    @TableField("status_des")
    private String statusDes;

    /**
     * 返利比例
     */
    @TableField("rate")
    private String rate;

    /**
     * 返利金额
     */
    @TableField("rebate")
    private String rebate;


}
