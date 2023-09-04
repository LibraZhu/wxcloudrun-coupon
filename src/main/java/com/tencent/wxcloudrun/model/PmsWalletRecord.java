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
 * 钱包交易记录表
 * </p>
 *
 * @author zjf
 * @since 2023年08月09日
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("pms_wallet_record")
public class PmsWalletRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 交易流水sn，yyyyMMddHHmmssSSS+3位随机数
     */
    @TableField("record_sn")
    private String recordSn;

    /**
     * 交易类型 1提现 2结算
     */
    @TableField("type")
    private Integer type;

    /**
     * 交易金额
     */
    @TableField("money")
    private String money;

    /**
     * 交易方式 0待定 1银行卡 2微信 3结算
     */
    @TableField("pay_type")
    private Integer payType;

    /**
     * 支付账号
     */
    @TableField("pay_account")
    private String payAccount;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 交易状态 0待支付 1成功 2失败
     */
    @TableField("pay_status")
    private Integer payStatus;

    /**
     * 交易时间
     */
    @TableField("pay_time")
    private LocalDateTime payTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 用户id
     */
    @TableField("uid")
    private Long uid;

    /**
     * 结算周期
     */
    @TableField("settle_period")
    private String settlePeriod;

    /**
     * 结算订单，多个英文逗号相隔
     */
    @TableField("settle_order_id")
    private String settleOrderId;


}
